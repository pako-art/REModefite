# REModefite

A **NeoForge 1.21.1** fork of [Modefite — Item Definition Backport](https://github.com/TimmyChips/Modefite-Item-Defintion-Backport)
by **TimmyChips**.

Modefite backports the item model definition system introduced in 1.21.4 —
`assets/<namespace>/items/*.json` with the `composite`, `condition`,
`range_dispatch` and `select` model types — so resource packs built for newer
versions can be used on 1.21.1.

This fork ports it from the Fabric sources and adds fixes on top. Licensed
**GPL-3.0**, same as upstream.

---

## Why this fork exists

TimmyChips publishes an official NeoForge build. This one differs in ways that
matter for large, modern pack sets.

**Two mixins the official build does not ship**, both present in the Fabric
sources:

- `ArmorTextureRedirectMixin` — redirects legacy worn-armor texture lookups to
  a pack's 1.21.2+ `equipment/` texture. Without it, armour from any pack built
  for a newer version does not appear.
- `GameRendererShaderResilienceMixin` — catches the `IOException` from a core
  shader that fails to compile and falls back to vanilla, instead of aborting
  the whole shader reload.

**Missing models no longer render as a magenta cube.** An item definition may
reference a model the pack does not ship, or one Minecraft cannot deserialise.
Registering such an id does not make the lookup fail: `ModelBakery` bakes the
missing model under it, and upstream draws a full-size magenta cube in hand.
Every referenced model is now validated through Minecraft's own deserialiser
during `RegisterAdditional`; ids that fail are never registered, so the resolver
falls back to the vanilla item model and logs the reason.

**Three defects fixed:**

- `UseKeyTracker` guarded its client-to-server send with `if (!world.isClient)`
  and then called `ClientPlayNetworking.send`, attempting the send only on the
  side with no client connection. The condition was inverted.
- `Registry.clear()` never cleared `rootDefinitions`, so after a resource reload
  `getRoot()` could answer with data from a pack set no longer active.
- `missingFallbackModel` dereferenced a parameter marked `@Nullable`.

**A display-context fix.** `ItemRenderer.getModel` carries no display context,
so upstream asked for `GUI` from every caller — including the hand renderer —
and compensated by re-rendering afterwards. That only holds while `renderStatic`
is on the path. The context is now recorded where it is actually known and used
directly.

**Per-frame work removed.** `range_dispatch` re-sorted its entry list for every
item on every frame, for an order fixed by the JSON; that sort moved into the
codec. Twelve warning sites built `"item|category"` strings before testing
whether the warning had already been logged. Neither was measured — both were
found by reading the code.

`PORTING-NOTES.md` documents all of it, including the attempts that were wrong.

---

## Compatibility

**Punchy** — `HeldItemSwapMixin` stands down automatically when Punchy is
installed. Both edit `ItemInHandRenderer.renderArmWithItem`, and with both
active the held item renders wrong. The only thing given up is
`hand_animation_on_swap`, and only while Punchy is present.

Third-party 3D models sometimes sit in the wrong place in hand with Punchy.
That is not a Modefite problem: a model's `display` transforms are calibrated
against a particular arm position, and Punchy moves the arm. Packs tuned for
Punchy work with it; packs tuned for vanilla work with mods that leave the arm
near its vanilla position. Punchy provides
`assets/minecraft/punchy/compat/item_tuning.json` for exactly this.

---

## Limits

- **Multi-axis element rotation is not supported.** `BlockElementRotation`
  holds one axis and one angle; Blockbench can export `{"x":…,"y":…,"z":…}` and
  vanilla has never read it. Models using it are skipped and log why. The
  rotation mixin only removes the 22.5°/45° restriction on the *angle*, which
  is a different thing.
- **Items and blocks that do not exist in 1.21.1** cannot be given a model. A
  pack built for 1.21.4+ will reference `pale_oak_sapling`, `resin_brick`,
  `copper_lantern` and so on.
- **Block model format changes and relocated GUI sprites** from 1.21.2+ are
  outside what an item-definition backport covers.
- Everything upstream lists as unimplemented still is: the `special`,
  `bundle/selected_item` and model tint source types.

---

## Building

```bash
./gradlew build -Dorg.gradle.java.home="<path to JDK 21>"
```

Gradle 8.14 rejects JDK 26 with `Unsupported class file major version 70`. The
JDK is passed on the command line rather than pinned in `gradle.properties`, so
no machine-specific path lands in the repo.

Output: `build/libs/Modefite-NeoForge-1.0.1+1.21.1.jar`

The `modId` remains `modefite`, so this cannot be installed alongside the
official build — it is a drop-in replacement. That is deliberate:
`ClientInitializer` derives the resource folder `<modid>_items_override` from
it, and renaming would silently break packs that use it.

---

## For resource pack authors

Everything upstream documents still applies — see the
[original wiki](https://github.com/TimmyChips/Modefite-Item-Defintion-Backport/wiki).
Custom properties and fields go in
`assets/<namespace>/modefite_items_override`.

Two things this fork adds to your log:

```
Skipping model <id>: <reason>. Item definitions referencing it fall back to the vanilla model.
Item definition points at a model that failed to load: <id>.
```

Both name the model and the reason, so a broken reference is findable instead of
appearing as a magenta cube in someone's hand.

---

## Credits

All original work is **TimmyChips'**. This fork changes what is listed above and
in `PORTING-NOTES.md`; everything else is theirs.

Bug reports about the original mod belong on
[their tracker](https://github.com/TimmyChips/Modefite-Item-Defintion-Backport/issues).
Issues specific to this fork belong here.

Licensed GPL-3.0. If you distribute a build of this, the source has to travel
with it.
