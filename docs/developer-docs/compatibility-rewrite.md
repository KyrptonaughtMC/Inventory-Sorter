# Compat Strategy

### Overview

The current compatibility system is fully client-controlled and only affects UI visibility and local sorting behavior. This rewrite aims to design a unified compatibility framework that supports:

- **Safe default behavior** across clients and servers
- **Extensibility** for mod developers and pack authors
- **Clarity** for players when sorting is disabled
- **Consistency** with similar inventory mods


#### Proposed Layers

1. **Mod Developer Hints**
    - Mods can annotate screens with e.g. `@NoSort` or register container capabilities
    - Tags: `#nosort`, `#nosortbutton`
    - Could integrate with Minecraft’s `DataComponent` or Fabric API

---

### Phase 3: Implementation

- Migrate compat list format to support tagging (`screenTags` instead of class names)
- Register tags via mod APIs or data files
- Add enforcement hooks server-side

---

### Long-Term

- Consider adopting (or contributing to) a cross-mod standard for inventory compatibility
- Detect presence of other sorting mods (e.g. MouseTweaks, Inventory Profiles) and align behavior where useful
- Expose API for mods to declare "sort-safe" zones programmatically

