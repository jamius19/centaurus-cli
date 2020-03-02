package com.jamiussiam.centaurus.structure;

public class FileParts {
    FileDetails parentFileDetails;
    int partNo;

    public FileParts(FileDetails parentFileDetails, int partNo) {
        this.parentFileDetails = parentFileDetails;
        this.partNo = partNo;
    }
}
