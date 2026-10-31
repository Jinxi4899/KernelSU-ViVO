#ifndef __KSU_H_SELINUX
#define __KSU_H_SELINUX

#include <linux/types.h>
#include <linux/version.h>
#include <linux/cred.h>

#define KERNEL_SU_DOMAIN "ksu"
#define KERNEL_SU_FILE "ksu_file"

#define KERNEL_SU_CONTEXT "u:r:" KERNEL_SU_DOMAIN ":s0"
#define KSU_FILE_CONTEXT "u:object_r:" KERNEL_SU_FILE ":s0"
#define ZYGOTE_CONTEXT "u:r:zygote:s0"
#define INIT_CONTEXT "u:r:init:s0"
#define ADBD_CONTEXT "u:r:adbd:s0"

void setup_selinux(const char *, struct cred *);

void setenforce(bool);

bool getenforce();

void cache_sid(void);

bool is_task_ksu_domain(const struct cred *cred);

bool is_ksu_domain();

bool is_zygote(const struct cred *cred);

bool is_init(const struct cred *cred);

bool is_adbd(const struct cred *cred);

void apply_kernelsu_rules();

int handle_sepolicy(void __user *user_data, u64 data_len);

void setup_ksu_cred();

void escape_to_root_for_adb_root();

extern u32 ksu_file_sid;

/*
 * Runtime-safe accessors for the SELinux inode security blob.
 *
 * On loadable-module builds with CONFIG_DEBUG_INFO_BTF the lbs_inode offset
 * is read from vmlinux BTF at init time (6.6 LTS moved it in
 * lsm_blob_sizes), and ksu_selinux_inode() verifies the blob back-reference
 * before returning it (NULL when absent, ERR_PTR on offset mismatch).
 * Built-in builds and kernels without BTF fall back to the compile-time
 * selinux_inode() access, which is correct there by construction.
 */
struct inode;
struct inode_security_struct;
int __init ksu_selinux_inode_init(void);
struct inode_security_struct *ksu_selinux_inode(const struct inode *inode);

#endif
