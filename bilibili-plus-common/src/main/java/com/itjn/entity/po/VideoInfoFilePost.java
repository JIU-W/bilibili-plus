package com.itjn.entity.po;

import lombok.Data;

import java.io.Serializable;


/**
 * 视频文件信息---发布表(发布时的视频文件信息)
 */
@Data
public class VideoInfoFilePost implements Serializable {

	/**
	 * 自增唯一ID
	 */
	private String fileId;

	/**
	 * 上传ID
	 */
	private String uploadId;

	/**
	 * 用户ID
	 */
	private String userId;

	/**
	 * 视频ID
	 */
	private String videoId;

	/**
	 * 文件索引
	 */
	private Integer fileIndex;

	/**
	 * 文件名
	 */
	private String fileName;

	/**
	 * 文件大小
	 */
	private Long fileSize;

	/**
	 * 文件路径
	 */
	private String filePath;

	/**
	 * 0:无更新 1:有更新
	 */
	private Integer updateType;

	/**
	 * 0:转码中 1:转码成功 2:转码失败
	 */
	private Integer transferResult;

	/**
	 * 持续时间（秒）
	 */
	private Integer duration;

	@Override
	public String toString (){
		return "自增唯一ID:"+(fileId == null ? "空" : fileId)+"，上传ID:"+(uploadId == null ? "空" : uploadId)+"，用户ID:"+(userId == null ? "空" : userId)+"，视频ID:"+(videoId == null ? "空" : videoId)+"，文件索引:"+(fileIndex == null ? "空" : fileIndex)+"，文件名:"+(fileName == null ? "空" : fileName)+"，文件大小:"+(fileSize == null ? "空" : fileSize)+"，文件路径:"+(filePath == null ? "空" : filePath)+"，0:无更新 1:有更新:"+(updateType == null ? "空" : updateType)+"，0:转码中 1:转码成功 2:转码失败:"+(transferResult == null ? "空" : transferResult)+"，持续时间（秒）:"+(duration == null ? "空" : duration);
	}

}
