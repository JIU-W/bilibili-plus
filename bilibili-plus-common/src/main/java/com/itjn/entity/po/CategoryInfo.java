package com.itjn.entity.po;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * 分类信息:对应数据库表：category_info
 */
@Data
public class CategoryInfo implements Serializable {


    /**
     * 自增分类ID
     */
    private Integer categoryId;

    /**
     * 分类编码
     */
    private String categoryCode;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 父级分类ID
     */
    private Integer pCategoryId;

    /**
     * 图标
     */
    private String icon;

    /**
     * 背景图
     */
    private String background;

    /**
     * 排序号
     */
    private Integer sort;

    public List<CategoryInfo> children = new ArrayList<>();




    public Integer getpCategoryId() {
        return this.pCategoryId;
    }


    @Override
    public String toString() {
        return "自增分类ID:" + (categoryId == null ? "空" : categoryId) + "，分类编码:" + (categoryCode == null ? "空" : categoryCode) + "，分类名称:" + (categoryName == null ? "空" : categoryName) + "，父级分类ID:" + (pCategoryId == null ? "空" : pCategoryId) + "，图标:" + (icon == null ? "空" : icon) + "，背景图:" + (background == null ? "空" : background) + "，排序号:" + (sort == null ? "空" : sort);
    }

}
