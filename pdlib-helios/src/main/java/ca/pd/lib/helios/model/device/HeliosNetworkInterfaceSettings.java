package ca.pd.lib.helios.model.device;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HeliosNetworkInterfaceSettings(
    String name,
    String mode,
    String address,
    String netmask,
    String gateway,
    List<HeliosIpAssignment> ipv4,
    @JsonProperty("ipv6_mode") String ipv6Mode,
    List<HeliosIpAssignment> ipv6,
    @JsonProperty("ipv6_gateway") String ipv6Gateway,
    HeliosDnsConfig dns,
    HeliosVlanConfig vlan,
    HeliosBondConfig bond,
    String mac,
    List<String> gateways) {}
