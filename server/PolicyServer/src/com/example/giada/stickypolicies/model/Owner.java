package com.example.giada.stickypolicies.model;

import java.math.BigInteger;
import java.util.Arrays;

public class Owner {
	private String referenceName;
	private String[] ownersDetails;
	private BigInteger certificateSN;

	protected Owner() {
	}

	public Owner(String referenceName, String[] ownersDetails, BigInteger certificateSN) {
		super();
		this.referenceName = referenceName;
		this.ownersDetails = ownersDetails;
		this.certificateSN = certificateSN;
	}

	public String getReferenceName() {
		return referenceName;
	}

	public String[] getOwnersDetails() {
		return ownersDetails;
	}

	public BigInteger getCertificateSN() {
		return certificateSN;
	}

	@Override
	public String toString() {
		return "Owner [referenceName=" + referenceName + ",\n OwnersDetails=" + Arrays.toString(ownersDetails)
				+ ",\n Certificate Serial Number=" + this.certificateSN + "]\n";
	}
}
