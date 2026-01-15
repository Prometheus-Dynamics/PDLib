package ca.pd.lib.helios.model.device;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HeliosCameraLayoutCamera(
    @JsonProperty("stream_id") String streamId,
    @JsonProperty("stream_alias") String streamAlias,
    @JsonProperty("camera_uid") String cameraUid,
    @JsonProperty("driver_camera_id") String driverCameraId,
    @JsonProperty("display_name") String displayName,
    String backend,
    @JsonProperty("hardware_id") String hardwareId,
    HeliosRigPose pose) {}
