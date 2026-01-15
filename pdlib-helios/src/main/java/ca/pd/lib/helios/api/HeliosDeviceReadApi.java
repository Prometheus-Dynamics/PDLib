package ca.pd.lib.helios.api;

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
import java.util.List;

/** Read-only typed device endpoints. */
public interface HeliosDeviceReadApi {
  HeliosHealth health() throws Exception;

  HeliosDeviceMetrics metrics() throws Exception;

  HeliosHostname hostname() throws Exception;

  List<HeliosNetworkInterfaceSettings> network() throws Exception;

  HeliosTeamNumber team() throws Exception;

  HeliosNt4Settings nt4() throws Exception;

  HeliosCameraLayout cameraLayout() throws Exception;

  HeliosDeviceLogs logs() throws Exception;

  List<HeliosLogSource> logSources() throws Exception;

  HeliosDeviceSnapshots snapshots() throws Exception;

  HeliosOsReleaseInfo osRelease() throws Exception;

  HeliosBootloaderStatus bootloaderStatus() throws Exception;

  HeliosResourceGuardStatus resourceGuardStatus() throws Exception;

  HeliosIpaStatus ipaStatus() throws Exception;
}
