package com.ruoyi.cms.controller.api;

import com.github.pagehelper.Page;
import com.ruoyi.common.base.AjaxResult;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.exam.service.IExamExaminationService;
import com.ruoyi.framework.jwt.JwtUtil;
import com.ruoyi.framework.web.exception.user.AuthExpireException;
import com.ruoyi.framework.web.util.ShiroUtils;
import com.ruoyi.system.domain.SysUser;
import com.ruoyi.system.service.ISysUserService;
import com.ruoyi.train.course.domain.Banner;
import com.ruoyi.train.course.domain.TrainCourse;
import com.ruoyi.train.course.domain.TrainCourseSearchHistory;
import com.ruoyi.train.course.domain.TrainCourseVO;
import com.ruoyi.train.course.service.IBannerService;
import com.ruoyi.train.course.service.ITrainCourseSearchHistoryService;
import com.ruoyi.train.course.service.ITrainCourseService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static com.ruoyi.common.base.AjaxResult.success;

/**
 * 首页相关API接口
 * @author liugang
 * @date 2020/02/04
 */
@Api("首页相关API")
@RestController
@RequestMapping("/api/v1")
public class ApiIndexController {
    @Autowired
    private IBannerService bannerService;
    @Autowired
    private ISysUserService sysUserService;
    @Autowired
    private ITrainCourseService trainCourseService;
    @Autowired
    private ITrainCourseSearchHistoryService trainCourseSearchHistoryService;

    /**
     * 获取首页信息
     *
     * @return
     */
    @GetMapping("/index")
    public AjaxResult index() {
        SysUser user = sysUserService.selectUserByLoginName( JwtUtil.getLoginName(), UserConstants.USER_VIP );
        if (ObjectUtils.isEmpty(user)){
            throw new AuthExpireException();
        }

        // banner 列表
        List<Banner> banners = bannerService.selectBanners();
        // 精选推荐课程列表
        List<TrainCourseVO> goodCourses = trainCourseService.selectGoodsCourses();
        // 最新推荐课程列表
        List<TrainCourseVO> newCourses = trainCourseService.selectNewCourses();
        Map<String, Object> data = new HashMap<>();
        data.put("banners",banners);
        data.put("goodCourses",goodCourses);
        data.put("newCourses",newCourses);
        AjaxResult success = success( "查询成功" );
        success.put("data",data);
        return success;
    }

    /**
     * 搜索
     *
     * @return
     */
    @GetMapping("/search")
    public AjaxResult search(@RequestParam("name") String name) {
        if (name == null || name.trim().isEmpty()){
            return AjaxResult.success();
        }

        TrainCourseVO trainCourseVO = new TrainCourseVO();
        trainCourseVO.setName(name.trim());
        trainCourseVO.setState("1");
        List<TrainCourseVO> list = trainCourseService.selectTrainCoursePage( trainCourseVO );
        AjaxResult success = success( "查询成功" );
        success.put("data",list);

        //搜索成功后增加搜索记录
        SysUser user = sysUserService.selectUserByLoginName( JwtUtil.getLoginName(), UserConstants.USER_VIP );
        if (!ObjectUtils.isEmpty(user)){
            TrainCourseSearchHistory searchHistory = new TrainCourseSearchHistory();
            searchHistory.setKeyword(name.trim());
            searchHistory.setDelFlag(0);
            searchHistory.setUserId(user.getUserId().toString());
            trainCourseSearchHistoryService.insert(searchHistory);
        }

        return success;
    }


    /**
     * 搜索历史，最多展示20个
     *
     * @return
     */
    @GetMapping("/searchHistory")
    public AjaxResult searchHistory() {
        SysUser user = sysUserService.selectUserByLoginName( JwtUtil.getLoginName(), UserConstants.USER_VIP );
        AjaxResult success = success( "查询成功" );

        if (!ObjectUtils.isEmpty(user)){
            success.put("data",new ArrayList<>());
        }
        List<TrainCourseSearchHistory> list = trainCourseSearchHistoryService.searchHistory(user.getUserId().toString());
        success.put("data",list);

        return success;
    }

    /**
     * 清除搜索历史
     *
     * @return
     */
    @PostMapping("/cleanSearchHistory")
    public AjaxResult cleanSearchHistory() {
        SysUser user = sysUserService.selectUserByLoginName( JwtUtil.getLoginName(), UserConstants.USER_VIP );
        if (ObjectUtils.isEmpty(user)){
            throw new AuthExpireException();
        }

        TrainCourseSearchHistory searchHistory = new TrainCourseSearchHistory();
        searchHistory.setUserId(user.getUserId().toString());
        trainCourseSearchHistoryService.delete(searchHistory);
        return AjaxResult.success("清除成功");
    }

    /**
     * 更多最新推荐
     * @param type 1、精选课程  2、最新推荐
     * @return
     */
    @GetMapping("/moreCourses")
    public AjaxResult moreGoodsCourses(@RequestParam("type") String type) {
        SysUser user = sysUserService.selectUserByLoginName( JwtUtil.getLoginName(), UserConstants.USER_VIP );
        if (ObjectUtils.isEmpty(user)){
            throw new AuthExpireException();
        }

        TrainCourseVO trainCourseVO = new TrainCourseVO();
        trainCourseVO.setState("1");
        if ("1".equals(type)){
            trainCourseVO.setIsGood(1);
        }else if ("2".equals(type)){
            trainCourseVO.setIsNew(1);
        }
        Map<String, Object> map = trainCourseService.selectMoreTrainCourses( trainCourseVO );
        List<TrainCourseVO> list = (List<TrainCourseVO>) map.get("list");
        Page page = (Page) map.get("page");
        AjaxResult success = success( "查询成功" );
        success.put("data",list);
        success.put("pages",page.getPages());
        return success;
    }
}
