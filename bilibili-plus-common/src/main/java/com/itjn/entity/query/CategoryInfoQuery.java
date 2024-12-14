package com.itjn.entity.query;


import lombok.Data;

/**
 * 分类信息参数
 */
@Data
public class CategoryInfoQuery extends BaseParam {


    /**
     * 自增分类ID
     */
    private Integer categoryId;

    /**
     * 分类编码
     */
    private String categoryCode;

    private String categoryCodeFuzzy;

    /**
     * 分类名称
     */
    private String categoryName;

    private String categoryNameFuzzy;

    /**
     * 父级分类ID
     */
    private Integer pCategoryId;

    /**
     * 图标
     */
    private String icon;

    private String iconFuzzy;

    /**
     * 背景图
     */
    private String background;

    private String backgroundFuzzy;

    /**
     * 排序号
     */
    private Integer sort;

    /**
     * 是否转换成树形结构
     */
    private Boolean convert2Tree;

    /**
     * 分类ID或父级分类ID
     */
    private Integer categoryIdOrPCategoryId;

}
