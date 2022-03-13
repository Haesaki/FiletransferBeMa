package com.sin.simplecloud4u.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class FileFolder implements Serializable {

    /**
     * 文件夹ID
     */
    private Integer fileFolderId;
    /**
     * 文件夹名称
     */
    private String fileFolderName;
    /**
     * 文件夹名称
     */
    private String fileFolderPath;
    /**
     * 父文件夹ID
     */
    private Integer parentFolderId;
    /**
     * 所属文件仓库ID
     */
    private Integer fileStoreId;
    /**
     * 创建时间
     */
    private Date time;
}
