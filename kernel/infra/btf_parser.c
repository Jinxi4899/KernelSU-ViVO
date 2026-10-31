// SPDX-License-Identifier: GPL-2.0-only
/*
 * Query struct member offsets from the kernel's own vmlinux BTF.
 *
 * Vendor kernels may reorder or insert members in shared structures
 * (e.g. 6.6 LTS inserting lbs_backing_file before lbs_inode in
 * lsm_blob_sizes). Hard-coded offsets compiled against one header layout
 * then silently point at the wrong blob and can corrupt memory or panic.
 * Reading the offset from vmlinux BTF at runtime keeps the access correct
 * for the running kernel.
 *
 * Only compiled for loadable-module builds on kernels with
 * CONFIG_DEBUG_INFO_BTF; built-in builds compile against the kernel's own
 * headers and never need this.
 */
#include "infra/btf_parser.h"

#if defined(MODULE) && defined(CONFIG_DEBUG_INFO_BTF)
#include <linux/bpf.h>
#include <linux/btf.h>
#include <linux/err.h>
#include <linux/errno.h>
#include <linux/string.h>

extern struct btf *btf_vmlinux;

int ksu_btf_field(const char *structure, const char *member, u32 kind, u32 size, struct ksu_btf_field *field)
{
    const struct btf *btf;
    const struct btf_type *type;
    const struct btf_member *m;
    s32 id;
    u32 i;

    btf = READ_ONCE(btf_vmlinux);
    if (IS_ERR_OR_NULL(btf))
        return btf ? PTR_ERR(btf) : -ENOENT;

    id = btf_find_by_name_kind(btf, structure, BTF_KIND_STRUCT);
    if (id < 0)
        return id;
    type = btf_type_by_id(btf, id);
    if (!type)
        return -EINVAL;

    for_each_member (i, type, m) {
        const struct btf_type *member_type;
        const char *name = btf_name_by_offset(btf, m->name_off);
        u32 bits, bytes;

        if (!name)
            return -EINVAL;
        if (strcmp(name, member))
            continue;
        bits = btf_member_bit_offset(type, m);
        bytes = bits / 8;
        if (btf_member_bitfield_size(type, m) || !size || (bits & 7) || bytes % size || bytes > type->size ||
            size > type->size - bytes)
            return -EINVAL;
        member_type = btf_type_skip_modifiers(btf, m->type, NULL);
        if (!member_type || BTF_INFO_KIND(member_type->info) != kind)
            return -EINVAL;
        if (kind == BTF_KIND_INT) {
            u32 info = *(const u32 *)(member_type + 1);

            if (member_type->size != size || BTF_INT_OFFSET(info) || BTF_INT_BITS(info) != size * 8 ||
                (BTF_INT_ENCODING(info) & (BTF_INT_CHAR | BTF_INT_BOOL)))
                return -EINVAL;
        } else if (kind != BTF_KIND_PTR || size != sizeof(void *)) {
            return -EINVAL;
        }
        field->offset = bytes;
        field->struct_size = type->size;
        return 0;
    }
    return -ENOENT;
}
#else
typedef int ksu_btf_parser_empty_translation_unit;
#endif
