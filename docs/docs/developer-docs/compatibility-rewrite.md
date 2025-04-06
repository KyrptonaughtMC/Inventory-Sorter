# Compat Strategy

### Overview

The current compatibility system is fully client-controlled and only affects UI visibility and local sorting behavior. This rewrite aims to design a unified compatibility framework that supports:

- **Safe default behavior** across clients and servers
- **Extensibility** for mod developers and pack authors
- **Clarity** for players when sorting is disabled
- **Consistency** with similar inventory mods

---

### Phase 1: Cleanup (Client-Only Authority) - DONE

- Remove server-side compatibility logic
- Drop the `/invsort` command-based config mutation for compat lists
- Store and apply all compatibility decisions purely on the client
- Add `CTRL+click` shortcut to auto-add screen class to config (locally)
- Optional: visual feedback or confirmation toast when compatibility is updated

---

### Phase 2: Design Unified Compat System - DONE

#### Key Questions

- What makes a container “unsafe” to sort?
- Who gets to decide: mod devs, server owners, or players?
- Should compatibility be explicit (opt-in) or implicit (assumed safe)?

#### Proposed Layers

1. **Mod Developer Hints**
    - Mods can annotate screens with e.g. `@NoSort` or register container capabilities
    - Tags: `#nosort`, `#nosortbutton`
    - Could integrate with Minecraft’s `DataComponent` or Fabric API

2. **Server-Side Enforcement** - DONE
    - Optional: server checks container compatibility before executing sort
    - Server may send a list of disallowed screens or tags
    - Sorting fails gracefully with a message:  
      _“Sorting is disabled for this container.”_

3. **Pack-Level Overrides** - DONE
    - External JSON compatibility lists
    - Layered merge priority: Mod defaults < Pack config < Player overrides
    - Configurable sync model: server pushes list, client respects and merges

4. **Player-Level Config** - DONE
    - GUI tools to block sorting on specific screens (already started)
    - Stored locally, applied only on client
    - Overrides any upstream compat if explicitly set

---

### Phase 3: Implementation

- Migrate compat list format to support tagging (`screenTags` instead of class names)
- Register tags via mod APIs or data files
- Add enforcement hooks server-side
- Build developer tools:
    - Live screen ID inspector
    - Auto-class detection for `CTRL+click`
    - Compat report generation for modpack builders

---

### Long-Term

- Consider adopting (or contributing to) a cross-mod standard for inventory compatibility
- Detect presence of other sorting mods (e.g. MouseTweaks, Inventory Profiles) and align behavior where useful
- Expose API for mods to declare "sort-safe" zones programmatically

