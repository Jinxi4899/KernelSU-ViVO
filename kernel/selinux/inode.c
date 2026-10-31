// SPDX-License-Identifier: GPL-2.0-only
/*
 * Runtime-safe access to the SELinux inode security blob.
 *
 * `selinux_inode()` from the kernel's objsec.h resolves to
 * `inode->i_security + selinux_blob_sizes.lbs_inode` with the offset baked in
 * at compile time. 6.6 LTS inserted lbs_backing_file before lbs_inode in
 * lsm_blob_sizes, so an LKM built against one header layout misreads the blob
 * on kernels with the other layout and can panic.
 *
 * On loadable-module builds with CONFIG_DEBUG_INFO_BTF we read the live
 * lbs_inode offset from vmlinux BTF instead, and verify the resolved blob by
 * its back-reference to the inode before handing it out. Built-in builds (and
 * kernels without BTF) keep using the compile-time access, which is correct
 * there by construction.
 */
#include <linux/err.h>
#include <linux/errno.h>
#include <linux/fs.h>
#include <linux/init.h>
#include <linux/kernel.h>
#include <linux/string.h>

#include "objsec.h"

#include "klog.h"
#include "selinux/selinux.h"
#include "infra/btf_parser.h"
#include "infra/symbol_resolver.h"

#if defined(MODULE) && defined(CONFIG_DEBUG_INFO_BTF)
static u32 inode_blob_offset __ro_after_init;

int __init ksu_selinux_inode_init(void)
{
    unsigned long blobs = find_kernel_symbol_exact("selinux_blob_sizes");
    struct ksu_btf_field blob_field;
    s32 blob_offset;
    int ret;

    if (!blobs) {
        pr_err("selinux_blob_sizes can not found!\n");
        return -ENOENT;
    }
    ret = ksu_btf_field("lsm_blob_sizes", "lbs_inode", BTF_KIND_INT, sizeof(int), &blob_field);
    if (ret)
        goto fail;
    memcpy(&blob_offset, (const void *)(blobs + blob_field.offset), sizeof(blob_offset));
    if (blob_offset < 0 || blob_offset % __alignof__(struct inode_security_struct)) {
        ret = -EINVAL;
        goto fail;
    }
    inode_blob_offset = blob_offset;
    pr_info("offset from vmlinux.btf, lbs_inode@%u, blob=%u\n", blob_field.offset, inode_blob_offset);
    return 0;
fail:
    pr_err("vmlinux BTF (%d) unreadable!\n", ret);
    return ret;
}
#else
int __init ksu_selinux_inode_init(void)
{
    return 0;
}
#endif

struct inode_security_struct *ksu_selinux_inode(const struct inode *inode)
{
#if defined(MODULE) && defined(CONFIG_DEBUG_INFO_BTF)
    struct inode_security_struct *security;

    if (!inode->i_security)
        return NULL;
    security = (void *)((char *)inode->i_security + inode_blob_offset);
    if (READ_ONCE(security->inode) != inode) {
        pr_err("Can not resolve inode_security_struct offset, current offset got: %d\n", inode_blob_offset);
        return ERR_PTR(-EINVAL);
    }
    return security;
#else
    return selinux_inode(inode);
#endif
}
