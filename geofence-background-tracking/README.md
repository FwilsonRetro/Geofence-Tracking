# geofence-background-tracking

Capacitor plugin to track a user's location through the use of geofences even if the application has been closed

## Install

```bash
npm install geofence-background-tracking
npx cap sync
```

## API

<docgen-index>

* [`echo(...)`](#echo)
* [`initializeGeofences()`](#initializegeofences)
* [`addListener('onEnter' | 'onExit', ...)`](#addlisteneronenter--onexit-)
* [Interfaces](#interfaces)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### echo(...)

```typescript
echo(options: { value: string; }) => Promise<{ value: string; }>
```

| Param         | Type                            |
| ------------- | ------------------------------- |
| **`options`** | <code>{ value: string; }</code> |

**Returns:** <code>Promise&lt;{ value: string; }&gt;</code>

--------------------


### initializeGeofences()

```typescript
initializeGeofences() => Promise<void>
```

--------------------


### addListener('onEnter' | 'onExit', ...)

```typescript
addListener(eventName: 'onEnter' | 'onExit', listenerFunc: (data: { identifier: string; }) => void) => Promise<PluginListenerHandle>
```

| Param              | Type                                                    |
| ------------------ | ------------------------------------------------------- |
| **`eventName`**    | <code>'onEnter' \| 'onExit'</code>                      |
| **`listenerFunc`** | <code>(data: { identifier: string; }) =&gt; void</code> |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt;</code>

--------------------


### Interfaces


#### PluginListenerHandle

| Prop         | Type                                      |
| ------------ | ----------------------------------------- |
| **`remove`** | <code>() =&gt; Promise&lt;void&gt;</code> |

</docgen-api>
