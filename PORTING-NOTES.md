# Porting notes — Modefite, Fabric → NeoForge 1.21.1

A fork of [Modefite — Item Definition Backport](https://github.com/TimmyChips/Modefite-Item-Defintion-Backport)
by **TimmyChips**, ported from the Fabric sources to NeoForge 1.21.1.

Licensed **GPL-3.0**, same as upstream. All original work is TimmyChips'; this
document lists what was changed and why.

TimmyChips publishes an official NeoForge build on Modrinth. That build was
used throughout as a reference implementation and as a control in testing —
several times it settled whether a defect came from this port or from the mod
itself. Where behaviour here differs from it, the difference is deliberate and
noted below.

---

## Building

```bash
./gradlew build -Dorg.gradle.java.home="<path to JDK 21>"
```

Gradle 8.14 rejects newer JDKs with `Unsupported class file major version 70`
(Java 26). The JDK is passed on the command line rather than pinned in
`gradle.properties`, so no machine-specific path lands in the repo.

---

## What changed structurally

| | Fabric | NeoForge |
|---|---|---|
| Build | fabric-loom 1.7 | ModDevGradle 2.0.141 |
| Source sets | `src/main` + `src/client` | `src/main` only, dist-restricted |
| Metadata | `fabric.mod.json` | `META-INF/neoforge.mods.toml`, `side = CLIENT` |
| Mixin configs | two, one empty | one |
| Refmap | `modefite.refmap.json` | none — NeoForge uses official mappings |
| Entrypoints | `ClientModInitializer` + `ModInitializer` | one `@Mod` class |

`ServerInitializer` is gone; NeoForge has a single entrypoint and this mod is
client-only regardless.

**`modId` stays `modefite`.** `ClientInitializer` derives the resource folder
`<modid>_items_override` from it, so renaming would silently break any pack
using that folder.

---

## Yarn → Mojmap

The mechanical part, applied across 76 files. The ones worth writing down are
those where the Yarn name actively misleads:

| Yarn | Mojmap |
|---|---|
| `Identifier` | `ResourceLocation` |
| `ModelTransformationMode` | `ItemDisplayContext` |
| `BuiltinModelItemRenderer` | `BlockEntityWithoutLevelRenderer` |
| `ComponentChanges` | `DataComponentPatch` |
| `NbtComponent` | `CustomData` |
| `BlockStateComponent` | `BlockItemStateProperties` |
| `VertexConsumerProvider` | `MultiBufferSource` |
| `MatrixStack` / `MatrixStack.Entry` | `PoseStack` / `PoseStack.Pose` |
| `ShaderProgram` | `ShaderInstance` |
| `Random` | `RandomSource` |
| `CustomPayload.Id` | `CustomPacketPayload.Type` (accessor `type()`, not `getId()`) |

Methods:

| Yarn | Mojmap |
|---|---|
| `ItemStack.getHolder()` | `getEntityRepresentation()` |
| `ItemStack.getMaxUseTime(e)` | `getUseDuration(e)` |
| `ItemStack.isOf(item)` / `isIn(tag)` | `is(...)` both |
| `ArmorTrim.getMaterial()` | `material()` — a `Holder`; the id comes from `unwrapKey().map(k -> k.location().toString())` |
| `CrossbowItem.getPullTime` | `getChargeDuration` |
| `KeyBinding.getTranslationKey` | `KeyMapping.getName` |
| `RegistryOps.of` | `RegistryOps.create` |
| `FluidState.getFluid()` | `getType()`; comparing two fluids is `isSame()`, since `is()` takes a `TagKey` |
| `AbstractContainerScreen` field `x` / `y` | `leftPos` / `topPos` — or just `getGuiLeft()` / `getGuiTop()`, which NeoForge makes public |
| `Options.getSyncedOptions().mainArm()` | `options.mainHand().get()` |

### Two traps

**`Vec3` has `x()/y()/z()`; `Entity` keeps `getX()/getY()/getZ()`.** A blanket
rename of either corrupts the other. This bit once, in `CompassFloat`.

**`BuiltInRegistries.ITEM.getId(item)` returns an `int` in Mojmap.** The
`ResourceLocation` comes from `getKey()`. Yarn's `getId()` means Mojmap's
`getKey()`, and the wrong one still compiles.

---

## What javac cannot check

Two classes of error survived a clean compile and only appeared at runtime.
Both cost a launch to find.

### Mixin string targets

`@Accessor` names, `method =` and `target =` are string literals. The
class-level remapping could not reach them, and the bulk passes left Yarn names
in place — some half-rewritten into paths that exist in neither mapping, like
`Lnet/minecraft/client/render/model/json/ItemDisplayContext;`, where the class
name is Mojmap and the package is Yarn.

The first launch crashed with:

```
InvalidAccessorException: No candidates were found matching x:I in
net/minecraft/client/gui/screens/inventory/AbstractContainerScreen
```

Every mixin was then audited against the real Mojmap classes, read out of the
NeoForm-compiled Minecraft jar rather than recalled:

| Yarn | Mojmap |
|---|---|
| `KeyBinding.KEYS_BY_ID` | `KeyMapping.ALL` |
| `ArmorMaterial.Layer.getTexture` | `texture` |
| `GameRenderer.loadPrograms` | `reloadShaders` |
| `BlockElement$Deserializer.deserializeRotationAngle` | `getAngle` |
| `ItemRenderer.renderItem(ItemStack, …)` | `render` |
| `ItemRenderer.renderItem(LivingEntity, …)` | **`renderStatic`** |
| `ItemRenderer.renderBakedItemModel` | `renderModelLists` |
| `ItemRenderer.builtinModelItemRenderer` | `blockEntityRenderer` |
| `ItemRenderer.getDirectItemGlintConsumer` | `getFoilBufferDirect` |
| `ItemInHandRenderer.renderFirstPersonItem` | `renderArmWithItem` |
| `ItemInHandRenderer.renderArmHoldingItem` | `renderPlayerArm` |
| `ItemInHandRenderer.applyEquipOffset` | `applyItemArmTransform` |

Note the two `renderItem` overloads: Yarn gives them one name, Mojmap gives
them **two different ones**. A uniform rename breaks one of them.

`usesDynamicDisplay` has no Mojmap counterpart on `ItemRenderer`, so it stopped
being a `@Shadow` and became a `@Unique` helper — the shadow already carried a
full body.

`HandleSlotAccessor` was deleted outright. It existed to read the protected
`x`/`y` fields; NeoForge exposes `getGuiLeft()`/`getGuiTop()` publicly.

### Silently wrong mappings

`MatrixUtil.scale(Matrix4f, float)` in Yarn maps to
`MatrixUtil.mulComponentWise`, not to JOML's `Matrix4f.scale()`. They are not
the same operation — `mulComponentWise` multiplies every component including
the translation column, while `scale()` applies a scaling transform that leaves
translation alone. Both compile. Held items rendered at the wrong size and
position until this was corrected.

The general lesson: a wrong mapping between two methods with compatible
signatures is invisible to the compiler and only shows up on screen.

---

## Loader glue

### Model loading

Fabric drove everything from one `ModelLoadingPlugin` callback: parse the item
definitions, then hand the model ids to `pluginContext.addModels`. NeoForge
splits this across two mod-bus events, and the split is load-bearing:

- ids must be declared during **`ModelEvent.RegisterAdditional`** or nothing is
  baked
- the baked instances only exist later, at **`ModelEvent.BakingCompleted`**

`FabricBakedModelManager.getModel(Identifier)` has no NeoForge counterpart:
standalone models are not reachable through the vanilla `ModelManager`. The
lookup is captured at bake time and installed into `ResolveRecursive` as a
`volatile Function`, replaced on every resource reload.

### Networking

`PayloadTypeRegistry` and `ServerPlayNetworking.registerGlobalReceiver` collapse
into one `RegisterPayloadHandlersEvent` registration, so a codec can no longer
drift from its handler. Sending goes through `PacketDistributor`.

`StreamCodec.tuple` does **not** work here: it requires all codecs to share one
buffer type, and `UUIDUtil`/`ByteBufCodecs` are `ByteBuf` while `ItemStack` is
`RegistryFriendlyByteBuf`. `composite()` takes `? super B` and does.

### Events

- `ClientTickEvents.END_CLIENT_TICK` and `END_WORLD_TICK` → both
  `ClientTickEvent.Post`; NeoForge has no separate client-world tick
- `UseItemCallback` → `PlayerInteractEvent.RightClickItem`

### Fabric rendering API

`isVanillaAdapter`, `emitItemQuads` and `emitBlockQuads` came from
`FabricBakedModel` and have no counterpart. Dropped rather than stubbed —
NeoForge renders through `getQuads` plus `IBakedModelExtension`, both already
implemented.

---

## Upstream defects fixed

**Inverted network side.** `UseKeyTracker` guarded the client-to-server send
with `if (!world.isClient)` and then called `ClientPlayNetworking.send` — it
only attempted the send while running on the server, which has no client
connection to send on. Now sends when `level.isClientSide`.

**`Registry.clear()` never cleared `rootDefinitions`.** It runs on every
resource reload, so `getRoot()` could answer with a definition from a pack set
that was no longer active.

**NPE in `missingFallbackModel`.** It dereferenced `type.getPath()` on a
parameter marked `@Nullable`, reachable whenever `property` is non-null and
`type` is null.

**Missing models drawn as a magenta cube.** This one took three attempts and
the failed ones are worth recording.

An item definition may reference a model the pack does not ship, or one
Minecraft cannot deserialise. Registering such an id does **not** make the
lookup return null: `ModelBakery` bakes the missing model under that id, so the
lookup succeeds and a full-size magenta cube is drawn in hand.

- *Attempt 1* — compare against `ModelManager.getMissingModel()` by identity.
  Missed it: the substituted instance is a different object.
- *Attempt 2* — also compare against the entry at
  `ModelBakery.MISSING_MODEL_VARIANT`. Missed it too; there is a third
  instance. The log made this unambiguous — the model failed to load and the
  check fired zero times.
- *Attempt 3* — treat any model whose particle icon is the missingno texture as
  missing. **Wrong in the other direction**: it rejected four models that had
  loaded perfectly well and merely referenced a texture the atlas could not
  resolve. A model with a missing texture is still a model.

The fix moves the question earlier, to where the answer is unambiguous. During
`RegisterAdditional`, every referenced model is read and run through
`BlockModel.fromStream` — Minecraft's own deserialiser, not a JSON
well-formedness test, because the failures that matter are Minecraft's own
(`Missing axis` on a Blockbench multi-axis rotation is perfectly valid JSON).
Ids that throw are never registered, so nothing is substituted for them, the
lookup returns null, and the resolver falls back to the vanilla item model.
Skipped ids are logged with the reason, which gives a pack author something
actionable.

Cost: one read and parse per referenced model at load. Nothing at render time.

---

## Restored from the Fabric sources

The official NeoForge build ships five mixins. This fork ships seven — six when
Punchy is installed. The two extra are the reason this fork exists:

- **`ArmorTextureRedirectMixin`** — redirects legacy worn-armor texture lookups
  to a pack's 1.21.2+ `equipment/` texture. Without it, armour from any pack
  built for a newer version does not appear.
- **`GameRendererShaderResilienceMixin`** — catches the `IOException` from a
  core shader that fails to compile and falls back to vanilla, instead of
  aborting the whole shader reload.

Both matter with a large modern pack set.

---

## Performance

`ResolveRecursive.resolve` runs per item, per frame, with no caching. Two things
on that path were doing real work every frame for a result that could not
change.

**The `range_dispatch` sort.**

```java
range.entries().stream()
    .sorted((a, b) -> Float.compare(b.threshold(), a.threshold()))
    .filter(...).findFirst()
```

An `O(n log n)` sort plus a stream and three lambdas, every frame, for an order
that depends only on the JSON thresholds. The sort moved into the codec, where
it happens once and yields an immutable list; the resolver walks it with a plain
loop.

**Warning keys.** Twelve call sites built `"item|category"` by concatenation
*before* testing the set, so each allocated two strings and an
`Item.toString()` per item per frame for a message logged once per session.
`warnOnce(category, stack)` keys a per-category set on the `Item` instance
instead; nothing is allocated after the first call.

Also: `new EmptyItemModel()` per resolve became a shared instance,
`property().toString().equals("minecraft:component")` became a
`ResourceLocation` comparison against a constant, and the linear scan of
`INVALID_MODEL_TYPES` — which called `getKey()` once per entry on a `Set` —
became one `getKey()` and one `contains()`.

**None of this was measured.** Every item above was found by reading the code,
not by profiling, and none has a number attached.

### Still open

- No caching anywhere. The result depends on the `ItemStack`, the display
  context and the entity, so a badly invalidated cache would produce wrong
  models rather than merely slow ones. Not worth attempting without measuring
  first.
- `new CompositeItemModel(...)` is allocated per resolve; it captures the stack
  and entity, so it is not trivially shareable.
- `ComponentCase` builds a `HashMap` with `String` keys per call, on the
  component-map predicate path.

---

## Display context

`ItemRenderer.getModel` carries no display context in its signature, so the
mixin on it asked for `GUI` from every caller — including the hand renderer.
Upstream compensated by intercepting `renderStatic` afterwards and re-rendering
with the right context, which only holds while `renderStatic` is on the path.

`ItemInHandRenderer.renderItem` does receive the context.
`HandDisplayContextMixin` records it at HEAD and clears it at RETURN;
`getModel` uses it when set and falls back to `GUI` when not, which is correct —
no hand on the path means no hand being drawn.

A plain static field rather than a `ThreadLocal`: written and read on the render
thread only, on a path that runs per item per frame.

---

## Punchy

`ModefiteMixinPlugin` skips `HeldItemSwapMixin` when `punchy` is present.

Both edit `ItemInHandRenderer.renderArmWithItem`: this mod uses `@ModifyArg` on
the `renderPlayerArm` and `applyItemArmTransform` calls inside it, while Punchy
cancels the vanilla arms there and installs its own transform baseline. With
both active the held item renders at the wrong size and position — the official
NeoForge build included. Removing this one mixin fixes it, verified by
isolation.

Mixin priority cannot resolve this: priority orders mixins sharing a target
class, and the rest of this mod patches `ItemRenderer`, which Punchy does not.

What is given up is narrow and only while Punchy is installed:
`hand_animation_on_swap` in an item definition stops being honoured.

### What is *not* a Modefite problem

Third-party 3D models sometimes sit in the wrong place in hand with Punchy.
This is not fixable from here, and the evidence is three-sided:

| | Weskerson's 3D Items | Drigo 3D Lanterns |
|---|---|---|
| **Punchy** | wrong | correct |
| **Hold My Items** | correct | — |

A model's `display` transforms are calibrated against a particular arm
position. Weskerson's `lantern_hand` declares
`firstperson_righthand translation [-2.5, 6.25, 4.5]` with a 114° rotation —
calibrated for vanilla. Drigo's declares `[0, 4, 1.5]` with no rotation —
calibrated for Punchy. Punchy moves the arm; Hold My Items leaves it near
vanilla. Each pack works with whichever matches.

Punchy solves this per item through
`assets/minecraft/punchy/compat/item_tuning.json`, which repositions
third-party models. Weskerson's ships only `tuning_template.json`, an unfilled
template — the author prepared for Punchy support and never completed it. No
mod can infer where Punchy wants a model to sit, and the values differ per
model, so shipping them from here would fix one pack and break another.

An earlier commit made this mod contribute no model in hand while Punchy was
installed. That was reverted: it fixed nothing and removed 3D held items
entirely, which is the whole point of these packs.

### Two dead mixins in Punchy

Worth reporting upstream, though neither is the cause of the above.
`punchy-2.7d` carries `ModefiteUseDurationMixin` and `ItemRendererMixin`
compiled into the jar and declared in no mixin config — verified across all 399
classes and all three of its configs. The first targets
`UseDurationFloat.getValue` and is clearly meant to arbitrate use-item
animations against this mod. Neither ever runs.

A `PunchyCompat` class briefly replicated the first one's apparent intent from
this side. It was removed: never verified to change anything, and
Punchy-specific behaviour compiled into a mod meant to ship standalone.

---

## Known limits

**Multi-axis element rotation is not supported**, and cannot be without
replacing Minecraft's data structures. `BlockElementRotation` holds one axis and
one angle; Blockbench can export `{"x": …, "y": …, "z": …}` and vanilla has
never read it. `UnlockedModelRotationDeserializerMixin` removes the
22.5°/45° restriction on the *angle*, which is a different thing. Roughly a
quarter of Weskerson's models use the multi-axis form and are skipped at load.

**Items and blocks that do not exist in 1.21.1** cannot be given a model. A pack
built for 1.21.4+ will reference `pale_oak_sapling`, `resin_brick`,
`copper_lantern` and so on; those warn and are skipped.

**Block model format changes and relocated GUI sprites** from 1.21.2+ are
outside what an item-definition backport covers.
