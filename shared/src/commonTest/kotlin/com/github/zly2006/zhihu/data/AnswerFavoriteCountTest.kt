/*
 * Zhihu++ - Free & Ad-Free Zhihu client for all platforms.
 * Copyright (C) 2024-2026, zly2006 <i@zly2006.me>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation (version 3 only).
 */

package com.github.zly2006.zhihu.data

import com.github.zly2006.zhihu.navigation.Article
import com.github.zly2006.zhihu.navigation.ArticleType
import com.github.zly2006.zhihu.viewmodel.feed.QUESTION_ANSWER_LIST_INCLUDE
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * 收藏数（`favlists_count` / `favorite_count`）只在 include 里显式请求时才会返回。
 *
 * 回归背景：详情接口默认响应不含该字段（见 real-api/answer-detail.json），模型取默认值 0，
 * 回答操作行的收藏数因此长期显示 0 且不易察觉。这里锁住两条请求契约。
 */
class AnswerFavoriteCountTest {
    @Test
    fun answerDetailIncludeRequestsFavoriteCount() {
        val include = zhihuContentDetailInclude(Article(type = ArticleType.Answer, id = 1L))

        assertTrue(
            include.split(",").contains("favlists_count"),
            "回答详情 include 必须包含 favlists_count，否则接口不返回收藏数：$include",
        )
    }

    @Test
    fun answerArticleIncludeRequestsFavoriteCount() {
        val include = zhihuContentDetailInclude(Article(type = ArticleType.Article, id = 1L))

        assertTrue(
            include.split(",").contains("favlists_count"),
            "文章详情 include 必须包含 favlists_count，否则接口不返回收藏数：$include",
        )
    }

    @Test
    fun questionAnswerListIncludeRequestsFavoriteCount() {
        val include = QUESTION_ANSWER_LIST_INCLUDE

        assertTrue(
            include.split(",").contains("favorite_count"),
            "问题回答列表 include 必须包含 favorite_count，否则卡片兜底拿不到收藏数：$include",
        )
    }
}
