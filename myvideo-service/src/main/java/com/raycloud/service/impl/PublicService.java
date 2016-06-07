package com.raycloud.service.impl;

import com.raycloud.dao.CategoryDao;
import com.raycloud.dao.VideoDao;
import com.raycloud.exception.ServiceException;
import com.raycloud.pojo.Category;
import com.raycloud.pojo.User;
import com.raycloud.pojo.Video;
import com.raycloud.request.UploadVideoRequest;
import com.raycloud.request.VideoGetRequest;
import com.raycloud.request.VideoListGetRequest;
import com.raycloud.response.ViewCategoryList;
import com.raycloud.response.ViewPreVideoList;
import com.raycloud.response.ViewVideoList;
import com.raycloud.util.ContentTypeUtil;
import com.raycloud.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * Created by linjunjie(490456661@qq.com) on 2016/6/3.
 */
@Service
public class PublicService {

    @Autowired
    private CategoryDao categoryDao;

    @Autowired
    private VideoDao videoDao;

    /**
     * 获得视频列表
     * @param request
     * @return
     * @throws ServiceException
     */
    public ViewVideoList getVideoList(VideoListGetRequest request)throws ServiceException{
        if(request.getPageNo() == null||request.getPageSize() == null){
            throw new ServiceException("页码/条数参数未传入",902);
        }
        Video video = new Video();
        video.setUsername(request.getUsername());
        video.setTitleLike(request.getTitle());
        video.setCategoryId(request.getCategoryId());
        video.setStartRow((request.getPageNo() - 1) * request.getPageSize());
        video.setPageSize(request.getPageSize());
        video.setStatus(1);
        video.orderByCreated(true);
        ViewVideoList viewVideoList = new ViewVideoList();
        viewVideoList.toVideoResponse(videoDao.getList(video));
        viewVideoList.setTotal(videoDao.getCount(video));
        return viewVideoList;
    }

    /**
     * 首页获取多个分类下的视频信息
     * @param request
     * @return
     * @throws ServiceException
     */
    public ViewPreVideoList getPreVideo(VideoListGetRequest request)throws ServiceException{
        //获取n个分类的视频信息，
        Category category = new Category();
        List<Category> categoryList = categoryDao.getList(category);
        Video video = new Video();
        video.setStartRow(0);
        video.setPageSize(request.getVideoNum());
        video.orderByCreated(true);
        video.setStatus(1);
        ViewPreVideoList viewPreVideoList = new ViewPreVideoList();
        for(int i=0;i < request.getCategoryNum()&&i<categoryList.size();i++){
            video.setCategoryId(categoryList.get(i).getId());
            List<Video> videos = videoDao.getList(video);
            viewPreVideoList.toResponse(videos);
        }
        return viewPreVideoList;
    }

    /**
     * 获取单一的视频信息。
     * @param request
     * @return
     * @throws ServiceException
     */
    public ViewVideoList.VideoListBean getVideo(VideoGetRequest request)throws ServiceException{
        ViewVideoList.VideoListBean videoListBean = new ViewVideoList.VideoListBean();
        Video video = new Video();
        video.setId(request.getId());
        video = videoDao.get(video);
        video.setStatus(1);
        videoListBean.setId(video.getId());
        videoListBean.setCategory(video.getCategory());
        videoListBean.setDesc(video.getDesc());
        videoListBean.setUrl(video.getUrl());
        videoListBean.setCreated(DateUtil.getDateTime(video.getCreated(),"yyyy-MM-dd HH:mm:ss"));
        videoListBean.setTitle(video.getTitle());
        videoListBean.setPic(video.getPic());
        videoListBean.setUsername(video.getUsername());
        return videoListBean;
    }

    /**
     * 获取分类列表
     * @return
     * @throws ServiceException
     */
    public ViewCategoryList getCategoryList()throws ServiceException{
        Category category = new Category();
        ViewCategoryList viewCategoryList = new ViewCategoryList();
        viewCategoryList.toCategoryResponse(categoryDao.getList(category));
        viewCategoryList.setTotal(categoryDao.getCount(category));
        return viewCategoryList;
    }

    /**
     * 上传视频文件
     * @param request
     * @throws ServiceException
     */
    public void uploadVideo(UploadVideoRequest request,User user)throws ServiceException{
        String filePath = DateUtil.getCurrentTime(user.getUsername())+ContentTypeUtil.getExtension(request.getPic().getContentType());
        String picPath =  DateUtil.getCurrentTime(user.getUsername())+ ContentTypeUtil.getExtension(request.getPic().getContentType());
        try {
            request.getVideo().transferTo(new File("/"+filePath));
            request.getPic().transferTo(new File("/"+picPath));
        } catch (IOException e) {
            throw new ServiceException("文件上传失败,请重新上传.",902);
        }
        Video video = new Video();
        video.setUsername(user.getUsername());
        video.setUrl(filePath);
        video.setCategoryId(request.getCategoryId());
        video.setTitle(request.getTitle());
        video.setCreated(new Date());
        video.setStatus(1);
        videoDao.insert(video);
    }





}
