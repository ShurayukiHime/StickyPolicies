package com.example.giada.stickypolicies.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class PKIPolicy {
	private String[] trustedAuthority;
	private Owner owner;
	private List<Policy> policies;
	private String encryptedText;

	public PKIPolicy(String[] trustedAuthority, String encryptedText) {
		super();
		this.trustedAuthority = trustedAuthority;
		this.owner = new Owner();
		this.policies = new ArrayList<Policy>();
		this.encryptedText = encryptedText;
	}

	public void addPolicy(String[] targets, Date validityDate, String[] constraints, String[] action) {
		this.policies.add(new Policy(targets, validityDate, constraints, action));
	}

	public void setOwner(String referenceName, String[] details) {
		this.owner = new Owner(referenceName, details);
	}

	public String[] getTrustedAuthority() {
		return trustedAuthority;
	}

	public Owner getOwner() {
		return owner;
	}

	public List<Policy> getPolicies() {
		return policies;
	}

	public String getEncryptedText() {
		return encryptedText;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Policy p : policies) {
			sb.append(p.toString());
		}
		return "PKIPolicy [trustedAuthority=" + Arrays.toString(trustedAuthority) + "\n, owner=" + owner.toString() + "\n, policies="
				+ sb.toString() + "\n, encryptedText=" + encryptedText + "]";
	}
}

class Owner {
	private String referenceName;
	private String[] ownersDetails;

	protected Owner() {
	}

	public Owner(String referenceName, String[] ownersDetails) {
		super();
		this.referenceName = referenceName;
		this.ownersDetails = ownersDetails;
	}

	public String getReferenceName() {
		return referenceName;
	}

	public String[] getOwnersDetails() {
		return ownersDetails;
	}

	@Override
	public String toString() {
		return "Owner [referenceName=" + referenceName + "\n, ownersDetails=" + Arrays.toString(ownersDetails) + "]\n";
	}
}

class Policy {
	private String[] target;
	private Date expirationDate;
	private String[] constraint;
	private String[] action;

	protected Policy() {
	}

	protected Policy(String[] target, Date expirationDate, String[] constraint, String[] action) {
		super();
		this.target = target;
		this.expirationDate = expirationDate;
		this.constraint = constraint;
		this.action = action;
	}

	public String[] getTarget() {
		return target;
	}

	public Date getExpirationDate() {
		return expirationDate;
	}

	public String[] getConstraint() {
		return constraint;
	}

	public String[] getAction() {
		return action;
	}

	@Override
	public String toString() {
		return "Policy [target=" + Arrays.toString(target) + "\n, expirationDate=" + expirationDate + "\n, constraint="
				+ Arrays.toString(constraint) + "\n, action=" + Arrays.toString(action) + "]\n";
	}
}