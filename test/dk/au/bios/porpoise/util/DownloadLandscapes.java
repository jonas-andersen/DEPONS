/*
 * Copyright (C) 2023 Jacob Nabe-Nielsen <jnn@bios.au.dk>
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public
 * License version 2 and only version 2 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if not, see 
 * <https://www.gnu.org/licenses>.
 * 
 * Linking DEPONS statically or dynamically with other modules is making a combined work based on DEPONS. 
 * Thus, the terms and conditions of the GNU General Public License cover the whole combination.
 * 
 * In addition, as a special exception, the copyright holders of DEPONS give you permission to combine DEPONS 
 * with free software programs or libraries that are released under the GNU LGPL and with code included in the 
 * standard release of Repast Simphony under the Repast Suite License (or modified versions of such code, with unchanged license). 
 * You may copy and distribute such a system following the terms of the GNU GPL for DEPONS and the licenses of the 
 * other code concerned.
 * 
 * Note that people who make modified versions of DEPONS are not obligated to grant this special exception for 
 * their modified versions; it is their choice whether to do so. 
 * The GNU General Public License gives permission to release a modified version without this exception; 
 * this exception also makes it possible to release a modified version which carries forward this exception.
 */

package dk.au.bios.porpoise.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import com.jgoodies.common.base.Objects;

public class DownloadLandscapes {

	private static final List<LandscapeFile> LANDSCAPES = List.of(
			new LandscapeFile("DanTysk", "DanTysk.zip", "https://depons.eu/files/landscapes/v3.2/DanTysk-95d6c9de.zip",
					5140915, "95d6c9de370c97ae9693629cc0ba19b590ed9b45e3496161fccf6f915a2c407b"),
			new LandscapeFile("Gemini", "Gemini.zip", "https://depons.eu/files/landscapes/v3.2/Gemini-0e17073a.zip",
					4851539, "0e17073acca3e1c9d610139226b75558e1aba2ed18912089c412de608e2526fa"),
			new LandscapeFile("Homogeneous", "Homogeneous.zip",
					"https://depons.eu/files/landscapes/v3.2/Homogeneous-c645a646.zip", 113457,
					"c645a646e132b404e25786c265cbc3cbec6b1f12b1e95a85f7f21d3efbc383e0"),
			new LandscapeFile("Kattegat", "Kattegat.zip",
					"https://depons.eu/files/landscapes/v3.2/Kattegat-2614f56a.zip", 38927699,
					"2614f56a771bf9c486a3186779040b2426b6027cfa4bb50aa61f9cd60ff94913"),
			new LandscapeFile("NorthSea", "NorthSea.zip",
					"https://depons.eu/files/landscapes/v3.2/NorthSea-906cac97.zip", 110193137,
					"906cac9741c818bc6bb16ac010847f9aff1aba536002cc017e1d0f90057b856d"));

	public static void main(String[] args) throws Exception {
		for (var landscape : LANDSCAPES) {
			System.out.print(landscape.name + ": ");
			try {
				landscape.downloadAndVerify(false);
				System.out.println("completed");
			} catch (IOException e) {
				System.out.println("failed: " + e.getMessage());
			}
		}
	}

	private static class LandscapeFile {
		public final String name;
		public final String filename;
		public final String url;
		public final long numBytes;
		public final String sha256;

		public LandscapeFile(String name, String filename, String url, long numBytes, String sha256) {
			this.name = name;
			this.filename = filename;
			this.url = url;
			this.numBytes = numBytes;
			this.sha256 = sha256;
		}

		void downloadAndVerify(boolean overwriteExisting) throws IOException, NoSuchAlgorithmException {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			File fOut = Paths.get("data", filename).toFile();

			if (fOut.exists() && !overwriteExisting) {
				throw new IOException("File " + filename + " exists and will not be overwritten");
			}

			try (BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());
					FileOutputStream out = new FileOutputStream(fOut)) {

				var buffer = new byte[4096];
				int actRead;
				while ((actRead = in.read(buffer, 0, 4096)) >= 0) {
					out.write(buffer, 0, actRead);
					digest.update(buffer, 0, actRead);
				}

				out.flush();
				if (fOut.length() != numBytes) {
					throw new IOException(
							"Incorrect size for file " + name + ". Is: " + fOut.length() + ". Expected: " + numBytes);
				}

				var readSha256 = digest.digest();
				var readSha256Str = bytesToHex(readSha256);
				if (!Objects.equals(sha256, readSha256Str)) {
					throw new IOException("Incorrect SHA256 checksum for file " + name);
				}
			}
		}

		private static String bytesToHex(byte[] hash) {
			StringBuilder hexString = new StringBuilder(2 * hash.length);
			for (int i = 0; i < hash.length; i++) {
				String hex = Integer.toHexString(0xff & hash[i]);
				if (hex.length() == 1) {
					hexString.append('0');
				}
				hexString.append(hex);
			}
			return hexString.toString();
		}

	}

}
