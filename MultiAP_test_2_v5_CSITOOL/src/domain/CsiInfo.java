package domain;

import com.google.gson.Gson;

import java.util.Arrays;

/**
 * 定义CSI数据包，主要是需要
 */
public class CsiInfo {
	//一个载波的CSI数据
	private String timestamp;
	private double ant1_amp;
	private double ant2_amp;
	private double ant3_amp;
	private double ant4_amp;
	private String mac;

	//30个载波的CSI数据数组
	private String[] total_timestamp;
	private double[] total_ant1_amp;
	private double[] total_ant2_amp;
	private double[] total_ant3_amp;
	private double[] total_ant4_amp;
	private String[] total_mac;


	@Override
	public String toString() {
		return  "{total_timestamp=" + Arrays.toString(total_timestamp) +
				", total_ant1_amp=" + Arrays.toString(total_ant1_amp) +
				", total_ant2_amp=" + Arrays.toString(total_ant2_amp) +
				", total_ant3_amp=" + Arrays.toString(total_ant3_amp) +
				", total_ant4_amp=" + Arrays.toString(total_ant4_amp) +
				", total_mac=" + Arrays.toString(total_mac) +
				'}';
	}

	public String toJsonString(CsiInfo csiInfo) {
		Gson gson = new Gson();
		String jsonObject = gson.toJson(csiInfo);
		return jsonObject;
	}

	public void set_timestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	public String get_timestamp() {
		return timestamp;
	}
	public void set_ant1_amp(double ant1_amp) {
		this.ant1_amp = ant1_amp;
	}
	public double get_ant1_amp() {
		return ant1_amp;
	}
	public void set_ant2_amp(double ant2_amp) {
		this.ant1_amp = ant2_amp;
	}
	public double get_ant2_amp() {
		return ant2_amp;
	}
	public void set_ant3_amp(double ant3_amp) {
		this.ant3_amp = ant3_amp;
	}
	public double get_ant3_amp() {
		return ant1_amp;
	}
	public void set_ant4_amp(double ant4_amp) {
		this.ant4_amp = ant4_amp;
	}
	public double get_ant4_amp() {
		return ant4_amp;
	}
	public void set_mac(String mac) {
		this.mac = mac;
	}
	public String get_mac() {
		return mac;
	}

	public void set_total_timestamp(String[] total_timestamp) {
		this.total_timestamp = total_timestamp;
	}
	public String[] set_total_timestamp() {
		return this.total_timestamp;
	}
	public void set_total_ant1_amp(double[] total_ant1_amp) {
		this.total_ant1_amp = total_ant1_amp;
	}
	public double[] get_total_ant1_amp() {
		return this.total_ant1_amp;
	}
	public void set_total_ant2_amp(double[] total_ant2_amp) {
		this.total_ant2_amp = total_ant2_amp;
	}
	public double[] get_total_ant2_amp() {
		return this.total_ant2_amp;
	}
	public void set_total_ant3_amp(double[] total_ant3_amp) {
		this.total_ant3_amp = total_ant3_amp;
	}
	public double[] get_total_ant3_amp() {
		return this.total_ant1_amp;
	}
	public void set_total_ant4_amp(double[] total_ant4_amp) {
		this.total_ant4_amp = total_ant4_amp;
	}
	public double[] get_total_ant4_amp() {
		return this.total_ant4_amp;
	}
	public void set_total_mac(String[] total_mac) {
		this.total_mac = total_mac;
	}
	public String[] set_total_mac() {
		return this.total_mac;
	}

}
