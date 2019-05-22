/*******************************************************************************
 * Copyright (C) 2019, VMware Inc
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package com.vmware.vmbk.soap.helpers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TokenExchangeRestHandler {

    @SuppressWarnings("rawtypes")
    public Map<Integer, String> httpPost(final URL url, final Map<String, String> headers, final String body)
	    throws Exception {

	final Map<Integer, String> responseMap = new HashMap<>();
	final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	conn.setDoOutput(true);
	conn.setRequestMethod("POST");
	final Iterator iterator = headers.entrySet().iterator();
	while (iterator.hasNext()) {
	    final Map.Entry pair = (Map.Entry) iterator.next();
	    conn.setRequestProperty(pair.getKey().toString(), pair.getValue().toString());
	    iterator.remove();
	}
	final OutputStream os = conn.getOutputStream();
	os.write(body.getBytes("UTF-8"));
	os.flush();

	final int responseCode = conn.getResponseCode();
	BufferedReader br = null;
	StringBuilder responseSB = null;
	try {
	    if (responseCode == HttpURLConnection.HTTP_OK) {
		final StringBuilder textBuilder = new StringBuilder();
		if (conn.getErrorStream() != null) {
		    final Reader reader = new BufferedReader(new InputStreamReader(conn.getErrorStream(),
			    Charset.forName(StandardCharsets.UTF_8.name())));
		    int c = 0;
		    while ((c = reader.read()) != -1) {
			textBuilder.append((char) c);
		    }
		}

		responseSB = new StringBuilder();
		br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String line;
		while ((line = br.readLine()) != null) {
		    responseSB.append(line);
		}
	    } else {
		throw new Exception("Failed : HTTP error code : " + responseCode);
	    }
	} catch (final Exception e) {
	    System.err.println("Failed : HTTP error code : " + responseCode);
	    responseMap.put(responseCode, "Failed : HTTP error code : ");
	    throw new Exception("Failed : HTTP error code : " + responseCode);
	} finally {
	    if (br != null) {
		br.close();
	    }
	    if (os != null) {
		os.close();
	    }
	}

	responseMap.put(responseCode, responseSB.toString());
	return responseMap;

    }

}
