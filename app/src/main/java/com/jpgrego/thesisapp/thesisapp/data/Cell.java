package com.jpgrego.thesisapp.thesisapp.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.telephony.CellIdentityGsm;
import android.telephony.CellInfoGsm;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.NeighboringCellInfo;
import android.telephony.gsm.GsmCellLocation;

/**
 * Created by jpgrego on 25-08-2016.
 */

/*
 * No real point in implementing equals, since CellID is, unfortunately, not guaranteed to be
 * correct
 */
public final class Cell implements Comparable<Cell>, Parcelable {

    public final int mcc, mnc, cid, lac, psc, dbm;
    public final String generation;
    private boolean isRegisteredCell = false;

    private Cell(Parcel in) {
        this.mcc = in.readInt();
        this.mnc = in.readInt();
        this.cid = in.readInt();
        this.lac = in.readInt();
        this.psc = in.readInt();
        this.dbm = in.readInt();
        this.generation = in.readString();
        this.isRegisteredCell = in.readByte() != 0;
    }

    public Cell(final int networkType, @NonNull final CellInfoGsm cellInfoGsm) {
        final CellIdentityGsm cellIdentityGsm;
        final CellSignalStrengthGsm cellSignalStrengthGsm;

        cellIdentityGsm = cellInfoGsm.getCellIdentity();
        cellSignalStrengthGsm = cellInfoGsm.getCellSignalStrength();

        this.mcc = cellIdentityGsm.getMcc();
        this.mnc = cellIdentityGsm.getMnc();
        this.cid = cellIdentityGsm.getCid();
        this.lac = cellIdentityGsm.getLac();
        //noinspection deprecation
        this.psc = cellIdentityGsm.getPsc();
        this.dbm = cellSignalStrengthGsm.getDbm();
        this.generation = CellUtils.getGenerationFromNetworkType(networkType);

        this.isRegisteredCell = cellInfoGsm.isRegistered();
    }

    public Cell(final int networkType, final int mcc, final int mnc, final int dbm,
                @NonNull final GsmCellLocation cellLocation) {
        this.mcc = mcc;
        this.mnc = mnc;
        this.cid = cellLocation.getCid();
        this.lac = cellLocation.getLac();
        this.psc = cellLocation.getPsc();
        this.dbm = dbm;
        this.generation = CellUtils.getGenerationFromNetworkType(networkType);
    }

    public Cell(final int networkType, final int mcc, final int mnc,
                @NonNull final NeighboringCellInfo neighboringCellInfo) {
        this.mcc = mcc;
        this.mnc = mnc;
        this.cid = neighboringCellInfo.getCid();
        this.lac = neighboringCellInfo.getLac();
        this.psc = neighboringCellInfo.getPsc();
        this.dbm = CellUtils.rssiToDbm(networkType, neighboringCellInfo.getRssi());
        this.generation = CellUtils.getGenerationFromNetworkType(networkType);
    }

    public static final Creator<Cell> CREATOR = new Creator<Cell>() {
        @Override
        public Cell createFromParcel(Parcel in) {
            return new Cell(in);
        }

        @Override
        public Cell[] newArray(int size) {
            return new Cell[size];
        }
    };

    public void setRegisteredCell() {
        isRegisteredCell = true;
    }

    public boolean isRegisteredCell() {
        return isRegisteredCell;
    }

    @Override
    public int compareTo(@NonNull Cell another) {
        return another.dbm - this.dbm;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(mcc);
        parcel.writeInt(mnc);
        parcel.writeInt(cid);
        parcel.writeInt(lac);
        parcel.writeInt(psc);
        parcel.writeInt(dbm);
        parcel.writeString(generation);
        parcel.writeByte((byte) (isRegisteredCell ? 1 : 0));
    }
}
