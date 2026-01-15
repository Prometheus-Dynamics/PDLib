# PDLib

 ```
 https://raw.githubusercontent.com/Prometheus-Dynamics/PDLib/refs/heads/main/vendordeps/PDLib-2026.json
 ```

## Vendordeps
Vendordeps live in `vendordeps/`:
- `vendordeps/PDLib-2026.json`

## Modules
- `pdlib-core`: shared utilities (currently just Jackson helpers).
- `pdlib-helios`: HeliOS integration: streams/pipelines/localization + NT4 + WebSockets + WPILib helpers.
- `pdlib-examples`: runnable examples for HeliOS usage.
- `pdlib-test`: live integration tests for read-only HeliOS API flow.

## HeliOS
- Entry point: `ca.pd.lib.helios.HeliOS`
- This repo now exposes a read-only API surface only (no write/set/mutate endpoints).
- Device read section: `cam.device()` (`HeliosDeviceReadApi`)
- Peripheral read section: `cam.peripherals()` (`cam.peripherials()` alias kept)
- Stream flow: `cam.streams()`, `cam.stream(uuid|alias|hardwareId)`
- Localization flow: `cam.localizations()`, `cam.localization(profileId|name)`, `cam.localization()`
- Pipeline flow: `cam.pipelines()`, `cam.pipeline(uuid|name|alias)`
- NT4 discovery: `ca.pd.lib.helios.nt4.HeliosNt4Device`
- WebSocket outputs: `ca.pd.lib.helios.ws.HeliosStreamOutputsSocket` (connect to `/v1/ws/streams/{id}/outputs`)
- WebSocket metrics: `ca.pd.lib.helios.ws.HeliosStreamMetricsSocket` (connect to `/v1/ws/streams/{id}/metrics`)

## Examples
Runnable example classes (with `main`) live under:
- `pdlib-examples/src/main/java/ca/pd/lib/helios/examples/`

Category folders:
- `.../examples/device/`: `HeliosDeviceTelemetryExample`, `HeliosFanExample`, `HeliosLightingExample`
- `.../examples/streams/`: `HeliosStreamLookupExample`
- `.../examples/pipelines/`: `HeliosPipelineOutputsExample`, `HeliosPipelinesLookupExample`, `HeliosStreamPipelineExample`
- `.../examples/localization/`: `HeliosLocalizationProfilesExample`, `HeliosLocalizationSolveExample`
- `.../examples/vision/`: `HeliosAprilTagDetectionsExample`
- `.../examples/pose/`: `HeliosSwervePoseEstimatorExample`, `HeliosDifferentialPoseEstimatorExample`

## Publishing (Local)
```bash
./gradlew publishToMavenLocal
```

## Publishing (Online)
Create a root `.env` file:
```bash
MAVEN_ONLINE_URL=https://maven.pdlib.local/PDLib
MAVEN_ONLINE_USER=<username>
MAVEN_ONLINE_PASSWORD=<password>
```

Then run:
```bash
./gradlew publishToMavenOnline
```

Optional overrides:
- `-PmavenOnlineUrl=...`
- `-PmavenOnlineUser=...`
- `-PmavenOnlinePassword=...`

## Live Tests (`pdlib-test`)
Run read-only integration tests against a specific target:
```bash
./gradlew :pdlib-test:test -Dhelios.it=true -Dhelios.target=172.31.250.1
```

Category folders:
- `pdlib-test/src/test/java/ca/pd/lib/test/integration/helios/`
- `pdlib-helios/src/test/java/ca/pd/lib/helios/integration/`

Target and timeout can also be set with Gradle properties:
- `-PheliosTarget=172.31.250.1`
- `-PheliosTimeoutSec=20`

Override versions with:
- `-PpdlibVersion=2026.0.0`
- `-PwpilibVersion=2026.+`
