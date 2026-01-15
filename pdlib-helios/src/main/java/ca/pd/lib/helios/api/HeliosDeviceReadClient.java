package ca.pd.lib.helios.api;

import ca.pd.lib.helios.http.HeliosHttp;
import ca.pd.lib.helios.model.HeliosNt4Settings;
import ca.pd.lib.helios.model.device.HeliosBootloaderStatus;
import ca.pd.lib.helios.model.device.HeliosCameraLayout;
import ca.pd.lib.helios.model.device.HeliosDeviceLogs;
import ca.pd.lib.helios.model.device.HeliosDeviceMetrics;
import ca.pd.lib.helios.model.device.HeliosDeviceSnapshots;
import ca.pd.lib.helios.model.device.HeliosHealth;
import ca.pd.lib.helios.model.device.HeliosHostname;
import ca.pd.lib.helios.model.device.HeliosIpaStatus;
import ca.pd.lib.helios.model.device.HeliosLogSource;
import ca.pd.lib.helios.model.device.HeliosNetworkInterfaceSettings;
import ca.pd.lib.helios.model.device.HeliosOsReleaseInfo;
import ca.pd.lib.helios.model.device.HeliosResourceGuardStatus;
import ca.pd.lib.helios.model.device.HeliosTeamNumber;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

/** HTTP-backed implementation of {@link HeliosDeviceReadApi}. */
public final class HeliosDeviceReadClient implements HeliosDeviceReadApi {
  private static final TypeReference<List<HeliosNetworkInterfaceSettings>> NETWORK_LIST_TYPE =
      new TypeReference<>() {};

  private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {};
  private static final TypeReference<List<HeliosLogSource>> LOG_SOURCES_TYPE = new TypeReference<>() {};

  private final HeliosHttp http;
  private final ObjectMapper mapper;

  public HeliosDeviceReadClient(HeliosHttp http) {
    this.http = Objects.requireNonNull(http, "http");
    this.mapper = http.mapper();
  }

  @Override
  public HeliosHealth health() throws Exception {
    return convert(http.getJson("/v1/health"), HeliosHealth.class);
  }

  @Override
  public HeliosDeviceMetrics metrics() throws Exception {
    return convert(http.getJson("/v1/device/metrics"), HeliosDeviceMetrics.class);
  }

  @Override
  public HeliosHostname hostname() throws Exception {
    return convert(http.getJson("/v1/device/hostname"), HeliosHostname.class);
  }

  @Override
  public List<HeliosNetworkInterfaceSettings> network() throws Exception {
    return convert(http.getJson("/v1/device/network"), NETWORK_LIST_TYPE);
  }

  @Override
  public HeliosTeamNumber team() throws Exception {
    return convert(http.getJson("/v1/device/team"), HeliosTeamNumber.class);
  }

  @Override
  public HeliosNt4Settings nt4() throws Exception {
    return HeliosNt4Settings.fromJson(http.getJson("/v1/device/nt4"));
  }

  @Override
  public HeliosCameraLayout cameraLayout() throws Exception {
    return convert(http.getJson("/v1/device/camera-layout"), HeliosCameraLayout.class);
  }

  @Override
  public HeliosDeviceLogs logs() throws Exception {
    List<String> lines = convert(http.getJson("/v1/device/logs"), STRING_LIST_TYPE);
    return new HeliosDeviceLogs(lines);
  }

  @Override
  public List<HeliosLogSource> logSources() throws Exception {
    return convert(http.getJson("/v1/device/logs/sources"), LOG_SOURCES_TYPE);
  }

  @Override
  public HeliosDeviceSnapshots snapshots() throws Exception {
    return convert(http.getJson("/v1/device/snapshots"), HeliosDeviceSnapshots.class);
  }

  @Override
  public HeliosOsReleaseInfo osRelease() throws Exception {
    return convert(http.getJson("/v1/device/os"), HeliosOsReleaseInfo.class);
  }

  @Override
  public HeliosBootloaderStatus bootloaderStatus() throws Exception {
    return convert(http.getJson("/v1/device/bootloader"), HeliosBootloaderStatus.class);
  }

  @Override
  public HeliosResourceGuardStatus resourceGuardStatus() throws Exception {
    return convert(http.getJson("/v1/device/resource-guard"), HeliosResourceGuardStatus.class);
  }

  @Override
  public HeliosIpaStatus ipaStatus() throws Exception {
    return convert(http.getJson("/v1/device/ipa"), HeliosIpaStatus.class);
  }

  private <T> T convert(JsonNode node, Class<T> type) throws IOException {
    return mapper.treeToValue(node, type);
  }

  private <T> T convert(JsonNode node, TypeReference<T> type) {
    return mapper.convertValue(node, type);
  }
}
