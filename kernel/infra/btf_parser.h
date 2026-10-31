/* SPDX-License-Identifier: GPL-2.0-only */
#ifndef KSU_BTF_PARSER_H
#define KSU_BTF_PARSER_H

#include <linux/errno.h>
#include <linux/types.h>

#if defined(MODULE) && defined(CONFIG_DEBUG_INFO_BTF)
#include <uapi/linux/btf.h>

struct ksu_btf_field {
    u32 offset;
    u32 struct_size;
};

/*
 * Resolve the byte offset (and struct size) of `member` inside the BTF struct
 * `structure`, using the kernel's own parsed vmlinux BTF. The member must be a
 * plain `kind` (BTF_KIND_INT with exactly `size` bytes, or BTF_KIND_PTR) --
 * bitfields and differently encoded members are rejected so callers never act
 * on a misparsed offset.
 */
int ksu_btf_field(const char *structure, const char *member, u32 kind, u32 size, struct ksu_btf_field *field);
#else
struct ksu_btf_field {
    u32 offset;
    u32 struct_size;
};

/* vmlinux BTF is unavailable (built-in build or kernel without BTF). */
static inline int ksu_btf_field(const char *structure, const char *member, u32 kind, u32 size,
                                struct ksu_btf_field *field)
{
    (void)structure;
    (void)member;
    (void)kind;
    (void)size;
    (void)field;
    return -EOPNOTSUPP;
}
#endif

#endif /* KSU_BTF_PARSER_H */
