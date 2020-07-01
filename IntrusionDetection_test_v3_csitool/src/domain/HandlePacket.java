package domain;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.Gson;
import util.Logger;


/**
 * 1.读取数据包
 * 2.获取CSI
 * 3.获取RSSI
 */
public class HandlePacket {
	/**
	 * 对应着read_bf_file文件
	 * @param bytes
	 * @return
	 */
	public static Packet readPacket(byte[] bytes) {

		ArrayList<Complex> complexs=new ArrayList<Complex>();
		Packet packet=new Packet();
		ArrayList<Complex> csia=new ArrayList<Complex>();
		ArrayList<Complex> csib=new ArrayList<Complex>();
		ArrayList<Complex> csic=new ArrayList<Complex>();
		HashMap<Integer,ArrayList<Complex>> csi=new HashMap<Integer,ArrayList<Complex>>();

		int[] perm = new int[3];
		perm[0] = ((bytes[16]) & 0x3) + 1;
		perm[1] = ((bytes[16] >> 2) & 0x3) + 1;
		perm[2] = ((bytes[16] >> 4) & 0x3) + 1;

		packet.setBfee_count(bytes[5] + (bytes[6] << 8));
		packet.setNtx(bytes[10]);
		packet.setNrx(bytes[9]);
		packet.setRssi_a(bytes[11]);
		packet.setRssi_b(bytes[12]);
		packet.setRssi_c(bytes[13]);
		packet.setAgc(bytes[15]);
		packet.setNoise(bytes[14]);
		packet.setPerm(perm);
//		Logger.i("getTimestamp_low" + packet.getTimestamp_low());
//		Logger.i("getBfee_count" + packet.getBfee_count());
//		Logger.i("getRssi_a" + packet.getRssi_a());
//		Logger.i("getRssi_b" + packet.getRssi_b());
		int len = 192;
		int[] payload = new int[len];
		for (int i = 0; i < len; i++) {
			//payload[i] = in.readUnsignedByte();
			payload[i] = (int)bytes[i+21] & 0xFF;
		}
		int index = 0;
		byte temp;

		for (int j = 0; j < 30; j++) {
			index += 3;
			int remainder = index % 8;
			for (int k = 0; k < 3; ++k) {
				Complex complex = new Complex();
				temp = (byte) ((payload[index / 8] >> remainder) | (payload[index / 8 + 1] << (8 - remainder)));
				complex.setReal(temp);

				temp = (byte) ((payload[index / 8 + 1] >> remainder) | (payload[index / 8 + 2] << (8 - remainder)));
				complex.setImage(temp);
				if (perm[k]==1)// correct the ordering
				{
					csia.add(complex);
				}else if(perm[k]==2)
				{
					csib.add(complex);
				}else{
					csic.add(complex);
				}

				complexs.add(complex);
				index += 16;
			}
		}
		csi.put(1, csia);
		csi.put(2, csib);
		csi.put(3, csic);
		packet.setCsi(csi);
		Gson gson = new Gson();
		//System.out.println("gson: "+gson.toJson(csi));
		return packet;

	}

	/**
	 * 对应着get_scaled_csi文件
	 * @param packet
	 * @return
	 */
		public static HashMap<Integer,ArrayList<Complex>> GetScaleCSI(Packet packet)
		{
			double csipow=0;
			double noise_db;
			ArrayList<Complex> scalcsia=new ArrayList<Complex>();
			ArrayList<Complex> scalcsib=new ArrayList<Complex>();
			ArrayList<Complex> scalcsic=new ArrayList<Complex>();
			HashMap<Integer,ArrayList<Complex>> scalcsi=new HashMap<Integer,ArrayList<Complex>>();

			for(int j=1;j<4;j++)
			{
				System.out.println(j);
				for(int i=0;i<30;i++){
					csipow+=Math.pow(packet.csi.get(j).get(i).getAmplitude(), 2);
				}
			}

			double rssipow=dbinv(getTotalRssi(packet));
			double scale = rssipow / (csipow / 30);

			  if (packet.noise == -127)
			  {
				  noise_db = -92;
			  }
			    else{

			     noise_db = packet.noise;
			    }
			    double thermal_noise_pwr = dbinv(noise_db);

			    double quant_error_pwr = scale * (packet.Nrx * packet.Ntx);

			    double total_noise_pwr = thermal_noise_pwr + quant_error_pwr;

			    for (int i=1;i<4;i++)
			    {
			    	for (int j=0;j<30;j++)
			    	{
			    		double re = packet.csi.get(i).get(j).RealPart * Math.sqrt(scale / total_noise_pwr);
			    		double im = packet.csi.get(i).get(j).ImagePart * Math.sqrt(scale / total_noise_pwr);
			    		if (i==1){
			    			scalcsia.add(new Complex(re,im));
			    		}
			    		if (i==2){
			    			scalcsib.add(new Complex(re,im));
			    		}
			    		if (i==3){
			    			scalcsic.add(new Complex(re,im));
			    		}
			    	}
			    }

			    scalcsi.put(1, scalcsia);
			    scalcsi.put(2, scalcsib);
			    scalcsi.put(3, scalcsic);

			    return scalcsi;


		}

	/**
	 * 对应着get_total_rss文件
	 * @param
	 * @return
	 */
		public static double getTotalRssi(Packet packet){
			double rssimag=0;
			if (packet.rssi_a!=0)
			{
				rssimag=rssimag+dbinv(packet.rssi_a);
			}
			if (packet.rssi_b!=0)
			{
				rssimag=rssimag+dbinv(packet.rssi_b);
			}
			if (packet.rssi_c!=0)
			{
				rssimag=rssimag+dbinv(packet.rssi_c);
			}

			return ((10*Math.log10(rssimag)+300)-300)-44-packet.agc;
		}


		public static double dbinv(double rssi){
			return Math.pow(10, rssi/10);

		}


}

