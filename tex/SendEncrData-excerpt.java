@Override
protected byte[] doInBackground(URL... urls) {
	String responseBody = null;
	URL searchUrl = urls[0];
	try {
		responseBody = NetworkUtils.getResponseFromHttpUrl(searchUrl, "POST", postData, applicationJsonContentType);
	} catch (IOException e) {
		e.printStackTrace();
		Log.d(TAG, "Error when sending encrypted POST: " + e.getMessage());
	}
	Gson gson = new Gson();
	return gson.fromJson(responseBody, byte[].class);
}
