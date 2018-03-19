package com.example.giada.stickypoliciesapp.policyModel;

/**
 * Created by Giada on 18/03/2018.
 */
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

    void setReferenceName(String referenceName) {
        this.referenceName = referenceName;
    }

    public String[] getOwnersDetails() {
        return ownersDetails;
    }

    void setOwnersDetails(String[] ownersDetails) {
        this.ownersDetails = ownersDetails;
    }

    public BigInteger getCertificateSN() {
        return certificateSN;
    }

    void setCertificateSN(BigInteger certificateSN) {
        this.certificateSN = certificateSN;
    }

    @Override
    public String toString() {
        return "Owner [referenceName=" + referenceName + ",\n OwnersDetails=" + Arrays.toString(ownersDetails)
                + ",\n Certificate Serial Number=" + this.certificateSN + "]\n";
    }
}