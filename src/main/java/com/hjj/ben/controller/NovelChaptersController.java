package com.hjj.ben.controller;

import com.hjj.ben.annotation.SystemControllerLog;
import com.hjj.ben.common.exception.MyCustomException;
import com.hjj.ben.model.NovelChapters;
import com.hjj.ben.model.NovelDetail;
import com.hjj.ben.service.INovelChaptersService;
import com.hjj.ben.service.INovelDetailService;
import com.hjj.ben.utils.ShellUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by ben on 6/22/17.
 */

@Controller
@RequestMapping("/chapters")
public class NovelChaptersController {

    private static final Logger logger = LoggerFactory.getLogger(NovelChaptersController.class);

    @Resource
    private INovelChaptersService novelChaptersService;
    @Resource
    private INovelDetailService novelDetailService;

    @Value("${novel.scrapy.path}")
    private String NOVEL_SCRAPY_PATH;
    @Value("${novel.path}")
    private String NOVEL_PATH;

    @RequestMapping(method = RequestMethod.GET, value = "/catalog/{novelDetailId}")
    @SystemControllerLog(description = "获取小说目录")
    public ModelAndView getCatalog(@PathVariable Integer novelDetailId,
                                   @RequestParam(value = "reverseFlag",
                                           defaultValue = "0") Integer reverseFlag,
                                   @RequestParam(value = "crawlFlag",
                                           defaultValue = "0") Integer crawlFlag) {
        // 测试自定义异常
        if (novelDetailId == -1) {
            throw new MyCustomException(500, "测试自定义异常");
        }

        if (crawlFlag == 1) {
            String basePath = this.getClass().getResource("/").getPath();
            logger.info("basePath: {}", basePath);

            String shellPath = basePath + "script/start_crawl.sh";
            logger.info("shellPath: {}", shellPath);

            logger.info("novel config :{}, {}", NOVEL_PATH, NOVEL_SCRAPY_PATH);

            ShellUtil shellUtil = new ShellUtil();
            shellUtil.startShell("sh " + shellPath + " " + NOVEL_PATH + " " + NOVEL_SCRAPY_PATH);
        }

        String reverseTag = reverseFlag == 0 ? "asc" : "desc";

        ModelAndView view = new ModelAndView("chapters/catalog");
        List<NovelChapters> list = novelChaptersService.getCatalogByDetailId(novelDetailId, reverseTag);
        view.addObject("catalogList", list);

        NovelDetail novelDetail = novelDetailService.getById(novelDetailId);
        view.addObject("novelDetail", novelDetail);

        view.addObject("reverseFlag", reverseFlag == 0 ? 1 : 0);
        return view;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{chapterId}")
    @SystemControllerLog(description = "获取小说章节内容")
    public ModelAndView getChapterById(@PathVariable Integer chapterId) {
        NovelChapters novelChapters = novelChaptersService.getChapterById(chapterId);
        ModelAndView view = new ModelAndView("chapters/chapter");
        view.addObject("chapter", novelChapters);

        NovelChapters lastChapter = novelChaptersService.getLastChapter(novelChapters.getResId(), novelChapters.getNovelDetailId());
        NovelChapters nextChapter = novelChaptersService.getNextChapter(novelChapters.getResId(), novelChapters.getNovelDetailId());

        view.addObject("lastChapter", lastChapter);
        view.addObject("nextChapter", nextChapter);

        return view;
    }

}
