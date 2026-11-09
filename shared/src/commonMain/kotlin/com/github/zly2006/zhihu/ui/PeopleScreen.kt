/*
 * Zhihu++ - Free & Ad-Free Zhihu client for all platforms.
 * Copyright (C) 2024-2026, zly2006 <i@zly2006.me>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation (version 3 only).
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.github.zly2006.zhihu.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import coil3.compose.AsyncImage
import com.fleeksoft.ksoup.Ksoup
import com.github.zly2006.zhihu.data.DataHolder
import com.github.zly2006.zhihu.data.FeedDisplayItem
import com.github.zly2006.zhihu.data.OfficialBadge
import com.github.zly2006.zhihu.data.ZhihuJson
import com.github.zly2006.zhihu.data.officialBadge
import com.github.zly2006.zhihu.data.officialBadgeDetails
import com.github.zly2006.zhihu.data.toFeedDisplayItemNavDestinationJson
import com.github.zly2006.zhihu.navigation.Account
import com.github.zly2006.zhihu.navigation.Article
import com.github.zly2006.zhihu.navigation.ArticleType
import com.github.zly2006.zhihu.navigation.CollectionContent
import com.github.zly2006.zhihu.navigation.LocalNavigator
import com.github.zly2006.zhihu.navigation.Person
import com.github.zly2006.zhihu.navigation.Pin
import com.github.zly2006.zhihu.navigation.Question
import com.github.zly2006.zhihu.navigation.withReadingQueueSource
import com.github.zly2006.zhihu.platform.rememberExternalUrlOpener
import com.github.zly2006.zhihu.platform.rememberImagePreviewOpener
import com.github.zly2006.zhihu.platform.rememberUserMessageSink
import com.github.zly2006.zhihu.platform.rememberZhihuWebUrlOpener
import com.github.zly2006.zhihu.reading.RegisterReadingQueueSource
import com.github.zly2006.zhihu.theme.ZhihuTypography
import com.github.zly2006.zhihu.ui.components.AuthorBadge
import com.github.zly2006.zhihu.ui.components.FeedCard
import com.github.zly2006.zhihu.ui.components.PageTurnTarget
import com.github.zly2006.zhihu.ui.components.PaginatedList
import com.github.zly2006.zhihu.ui.components.ProgressIndicatorFooter
import com.github.zly2006.zhihu.ui.components.pageTurnViewportWithGuide
import com.github.zly2006.zhihu.ui.components.rememberPageTurnTarget
import com.github.zly2006.zhihu.ui.designsystem.ZhihuShapeTokens
import com.github.zly2006.zhihu.ui.designsystem.ZhihuSizes
import com.github.zly2006.zhihu.ui.designsystem.ZhihuSpacing
import com.github.zly2006.zhihu.ui.designsystem.contentFirstColors
import com.github.zly2006.zhihu.util.Log
import com.github.zly2006.zhihu.util.formatCompactCount
import com.github.zly2006.zhihu.util.formatRelativeTime
import com.github.zly2006.zhihu.util.jsonObject
import com.github.zly2006.zhihu.util.raiseForStatus
import com.github.zly2006.zhihu.viewmodel.ContentBlocklistEnvironment
import com.github.zly2006.zhihu.viewmodel.PaginationEnvironment
import com.github.zly2006.zhihu.viewmodel.PaginationViewModel
import com.github.zly2006.zhihu.viewmodel.ProfileLoadEnvironment
import com.github.zly2006.zhihu.viewmodel.ZhihuApiEnvironment
import com.github.zly2006.zhihu.viewmodel.addReadHistory
import com.github.zly2006.zhihu.viewmodel.deleteSigned
import com.github.zly2006.zhihu.viewmodel.feed.BaseFeedViewModel
import com.github.zly2006.zhihu.viewmodel.postSigned
import com.github.zly2006.zhihu.viewmodel.rememberPaginationEnvironment
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.compose.resources.painterResource
import zhihu.shared.generated.resources.Res
import zhihu.shared.generated.resources.ic_zh_plus_author_badge
import kotlin.math.roundToInt
import kotlin.reflect.typeOf
import androidx.lifecycle.viewmodel.compose.viewModel as composeViewModel
import com.github.zly2006.zhihu.navigation.Search as SearchDestination

class PeopleAnswersViewModel(
    val person: Person,
) : PaginationViewModel<DataHolder.Answer>(
        typeOf<DataHolder.Answer>(),
    ) {
    var sortBy by mutableStateOf("voteups")
        private set

    override val initialUrl: String
        get() = "https://www.zhihu.com/api/v4/members/${person.userTokenOrId}/answers?sort_by=$sortBy"

    override val include: String
        get() = "data[*].is_normal,admin_closed_comment,reward_info,is_collapsed,annotation_action,annotation_detail,collapse_reason,collapsed_by,suggest_edit,comment_count,thanks_count,can_comment,content,editable_content,attachment,voteup_count,reshipment_settings,comment_permission,created_time,updated_time,review_info,excerpt,paid_info,reaction_instruction,is_labeled,label_info,relationship.is_authorized,voting,is_author,is_thanked,is_nothelp,author.badge_v2"

    fun updateSortBy(newSort: String): Boolean {
        if (sortBy == newSort) {
            return false
        }
        sortBy = newSort
        return true
    }

    fun changeSortBy(newSort: String, environment: PaginationEnvironment) {
        if (updateSortBy(newSort)) {
            refresh(environment)
        }
    }
}

class PeopleArticlesViewModel(
    val person: Person,
) : PaginationViewModel<DataHolder.Article>(
        typeOf<DataHolder.Article>(),
    ) {
    var sortBy by mutableStateOf("created")
        private set

    override val initialUrl: String
        get() = "https://www.zhihu.com/api/v4/members/${person.userTokenOrId}/articles?sort_by=$sortBy"

    override val include: String
        get() = "data[*].comment_count,suggest_edit,is_normal,thumbnail_extra_info,thumbnail,can_comment,comment_permission,admin_closed_comment,content,voteup_count,created,updated,upvoted_followees,voting,review_info,reaction_instruction,is_labeled,label_info,author.badge_v2;data[*].vessay_info;data[*].author.badge[?(type=best_answerer)].topics;"

    fun updateSortBy(newSort: String): Boolean {
        if (sortBy == newSort) {
            return false
        }
        sortBy = newSort
        return true
    }

    fun changeSortBy(newSort: String, environment: PaginationEnvironment) {
        if (updateSortBy(newSort)) {
            refresh(environment)
        }
    }
}

class PeopleActivitiesViewModel(
    val person: Person,
    val sort: String = "created",
) : BaseFeedViewModel() {
    override val initialUrl: String
        get() = "https://www.zhihu.com/api/v3/moments/${person.userTokenOrId}/activities"
}

class PeopleFollowersViewModel(
    val person: Person,
) : PaginationViewModel<DataHolder.People>(
        typeOf<DataHolder.People>(),
    ) {
    override val initialUrl: String
        // 签名有bug，暂时无法使用新的API，先回退到旧的API
        // get() = "https://www.zhihu.com/api/v4/members/${person.userTokenOrId}/followers"
        get() = "https://api.zhihu.com/people/${person.id}/followers"

    override val include: String
        get() = "data[*].answer_count,articles_count,gender,follower_count,is_followed,is_following,badge_v2,badge[?(type=best_answerer)].topics"
}

class PeopleFollowingViewModel(
    val person: Person,
) : PaginationViewModel<DataHolder.People>(
        typeOf<DataHolder.People>(),
    ) {
    override val initialUrl: String
        get() = "https://www.zhihu.com/api/v4/members/${person.userTokenOrId}/followees"

    override val include: String
        get() = "data[*].answer_count,articles_count,gender,follower_count,is_followed,is_following,badge_v2,badge[?(type=best_answerer)].topics"
}

class PeopleCollectionsViewModel(
    val person: Person,
) : PaginationViewModel<DataHolder.Collection>(
        typeOf<DataHolder.Collection>(),
    ) {
    override val initialUrl: String
        get() = "https://www.zhihu.com/api/v4/members/${person.userTokenOrId}/favlists"

    override val include: String
        get() = "data[*].updated_time,answer_count,follower_count,creator"
}

class PeopleQuestionsViewModel(
    val person: Person,
) : PaginationViewModel<DataHolder.Question>(
        typeOf<DataHolder.Question>(),
    ) {
    override val initialUrl: String
        get() = "https://www.zhihu.com/api/v4/members/${person.userTokenOrId}/questions"

    override val include: String
        get() = "data[*].created,answer_count,follower_count,author,visit_count,comment_count,detail,relationship,topics,voteup_count"
}

class PeoplePinsViewModel(
    val person: Person,
) : PaginationViewModel<DataHolder.Pin>(
        typeOf<DataHolder.Pin>(),
    ) {
    override val initialUrl: String
        get() = "https://www.zhihu.com/api/v4/v2/pins/${person.userTokenOrId}/moments"

    override val include: String
        get() = "data[*].like_count,comment_count,created,updated,content"
}

class PeopleColumnContributionsViewModel(
    val person: Person,
) : PaginationViewModel<DataHolder.Column>(
        typeOf<DataHolder.Column>(),
    ) {
    override val initialUrl: String
        get() = "https://www.zhihu.com/api/v4/members/${person.userTokenOrId}/column-contributions"

    override val include: String
        get() = "data[*].articles_count,followers,author"
}

class PeopleFollowingCollectionsViewModel(
    val person: Person,
) : PaginationViewModel<DataHolder.Collection>(
        typeOf<DataHolder.Collection>(),
    ) {
    override val initialUrl: String
        get() = "https://www.zhihu.com/api/v4/members/${person.userTokenOrId}/following-favlists"

    override val include: String
        get() = "data[*].updated_time,answer_count,follower_count,creator"
}

@Serializable
data class FollowedQuestion(
    val id: String,
    val type: String = "question",
    val url: String = "",
    val title: String = "",
    val questionType: String = "",
    val created: Long = 0L,
    val updatedTime: Long = 0L,
)

@Serializable
data class FollowedTopic(
    val id: String = "",
    val type: String = "topic",
    val url: String = "",
    val name: String = "",
    val avatarUrl: String? = null,
    val topicType: String? = null,
    val topic: DataHolder.Topic? = null,
) {
    val displayId: String get() = topic?.id ?: id
    val displayName: String get() = topic?.name ?: name
    val displayAvatarUrl: String? get() = topic?.avatarUrl ?: avatarUrl
}

class PeopleFollowingQuestionsViewModel(
    val person: Person,
) : PaginationViewModel<FollowedQuestion>(
        typeOf<FollowedQuestion>(),
    ) {
    override val initialUrl: String
        get() = "https://www.zhihu.com/api/v4/members/${person.userTokenOrId}/following-questions"

    override val include: String
        get() = ""
}

class PeopleFollowingTopicsViewModel(
    val person: Person,
) : PaginationViewModel<FollowedTopic>(
        typeOf<FollowedTopic>(),
    ) {
    override val initialUrl: String
        get() = "https://www.zhihu.com/api/v4/members/${person.userTokenOrId}/following-topic-contributions"

    override val include: String
        get() = ""
}

class PeopleFollowingColumnsViewModel(
    val person: Person,
) : PaginationViewModel<DataHolder.Column>(
        typeOf<DataHolder.Column>(),
    ) {
    override val initialUrl: String
        get() = "https://www.zhihu.com/api/v4/members/${person.userTokenOrId}/following-columns"

    override val include: String
        get() = "data[*].articles_count,followers,author"
}

class PersonViewModel(
    val person: Person,
) : ViewModel() {
    var avatar by mutableStateOf("")
    var name by mutableStateOf(person.name)
    var headline by mutableStateOf("")
    var officialBadge by mutableStateOf<OfficialBadge?>(null)
    var officialBadgeDetails by mutableStateOf<List<OfficialBadge>>(emptyList())
    var githubSocial by mutableStateOf<GithubSocialUiState?>(null)
    var followerCount by mutableIntStateOf(0)
    var followingCount by mutableIntStateOf(0)
    var answerCount by mutableIntStateOf(0)
    var articleCount by mutableIntStateOf(0)
    var isFollowing by mutableStateOf(false)
    var isBlocking by mutableStateOf(false)
    var isBlockedInRecommendations by mutableStateOf(false)
    var isBlockedAsQuestionAuthor by mutableStateOf(false)
    var memberHashId by mutableStateOf(person.id)

    // 只实现已有数据类型的 ViewModel
    val answersFeedModel = PeopleAnswersViewModel(person)
    val articlesFeedModel = PeopleArticlesViewModel(person)
    val activitiesFeedModel = PeopleActivitiesViewModel(person)
    val collectionsFeedModel = PeopleCollectionsViewModel(person)
    val questionsFeedModel = PeopleQuestionsViewModel(person)
    val pinsFeedModel = PeoplePinsViewModel(person)
    val columnsFeedModel = PeopleColumnContributionsViewModel(person)
    val followersFeedModel = PeopleFollowersViewModel(person)
    val followingFeedModel = PeopleFollowingViewModel(person)
    val followingCollectionsFeedModel = PeopleFollowingCollectionsViewModel(person)
    val followingQuestionsFeedModel = PeopleFollowingQuestionsViewModel(person)
    val followingTopicsFeedModel = PeopleFollowingTopicsViewModel(person)
    val followingColumnsFeedModel = PeopleFollowingColumnsViewModel(person)
    val subFeedModels = arrayOf(
        answersFeedModel,
        articlesFeedModel,
        activitiesFeedModel,
        collectionsFeedModel,
        questionsFeedModel,
        pinsFeedModel,
        columnsFeedModel,
        followersFeedModel,
        followingFeedModel,
    )

    suspend fun toggleFollow(environment: ZhihuApiEnvironment) {
        val followersUrl = "https://www.zhihu.com/api/v4/members/${person.urlToken}/followers"
        val newFollowingState = !isFollowing
        val response = if (newFollowingState) {
            environment.postSigned(followersUrl)
        } else {
            environment.deleteSigned(followersUrl)
        }
        val jojo = response.raiseForStatus().jsonObject()
        followerCount = jojo["follower_count"]?.jsonPrimitive?.int ?: (followerCount + if (newFollowingState) 1 else -1)
        isFollowing = newFollowingState
    }

    suspend fun toggleBlock(environment: ZhihuApiEnvironment) {
        val blockUrl = "https://www.zhihu.com/api/v4/members/${person.urlToken}/actions/block"
        val newBlockingState = !isBlocking
        if (newBlockingState) {
            environment.postSigned(blockUrl)
        } else {
            environment.deleteSigned(blockUrl)
        }.raiseForStatus()
        isBlocking = newBlockingState
    }

    suspend fun toggleRecommendationBlock(environment: ContentBlocklistEnvironment) {
        if (isBlockedInRecommendations) {
            environment.removeBlockedUser(person.id)
            isBlockedInRecommendations = false
        } else {
            environment.addBlockedUser(
                userId = person.id,
                userName = name,
                urlToken = person.urlToken,
                avatarUrl = avatar,
            )
            isBlockedInRecommendations = true
        }
    }

    suspend fun toggleQuestionAuthorBlock(environment: ContentBlocklistEnvironment) {
        if (isBlockedAsQuestionAuthor) {
            environment.removeBlockedQuestionAuthor(person.id)
            isBlockedAsQuestionAuthor = false
        } else {
            environment.addBlockedQuestionAuthor(
                userId = person.id,
                userName = name,
                urlToken = person.urlToken,
                avatarUrl = avatar,
            )
            isBlockedAsQuestionAuthor = true
        }
    }

    suspend fun load(environment: ProfileLoadEnvironment) {
        environment.addReadHistory(person.id, "profile")

        val profileUrl = "https://api.zhihu.com/people/${person.urlToken.takeIf(String::isNotBlank) ?: person.id}"
        val jojo = environment.fetchJson(profileUrl, PEOPLE_PROFILE_INCLUDE_PATH)
            ?: error("用户资料为空")

        val loadedPerson = ZhihuJson.decodeJson<DataHolder.People>(jojo)
        val urlToken = loadedPerson.urlToken

        environment.postHistoryDestination(
            Person(
                id = loadedPerson.id,
                name = loadedPerson.name,
                urlToken = urlToken ?: "",
            ),
        )

        this.avatar = loadedPerson.avatarUrl
        this.name = loadedPerson.name
        this.headline = loadedPerson.headline
        this.officialBadge = loadedPerson.badgeV2.officialBadge()
        this.officialBadgeDetails = loadedPerson.badgeV2.officialBadgeDetails()
        this.followerCount = loadedPerson.followerCount
        this.followingCount = loadedPerson.followingCount
        this.answerCount = loadedPerson.answerCount
        this.articleCount = loadedPerson.articlesCount
        this.isFollowing = loadedPerson.isFollowing
        this.isBlocking = loadedPerson.isBlocking
        this.isBlockedInRecommendations = environment.isUserBlocked(loadedPerson.id)
        this.isBlockedAsQuestionAuthor = environment.isQuestionAuthorBlocked(loadedPerson.id)
        this.memberHashId = loadedPerson.id
        this.person.id = loadedPerson.id
        if (urlToken != null) {
            this.person.urlToken = urlToken
        }

        this.githubSocial = try {
            val detailUrl =
                "https://api.zhihu.com/people/${person.urlToken.takeIf(String::isNotBlank) ?: person.id}/profile/detail"
            environment
                .fetchJson(detailUrl, "")
                ?.let { ZhihuJson.decodeJson<DataHolder.People>(it).githubSocialUiState() }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            Log.e("PersonViewModel", "Failed to load optional social media profile detail", error)
            null
        }
    }
}

private fun DataHolder.Answer.toPeopleDestination() = Article(
    type = ArticleType.Answer,
    id = id,
    title = question.title,
    authorName = author.name,
    authorBio = author.headline,
    avatarSrc = author.avatarUrl,
    excerpt = excerpt,
)

private fun DataHolder.Article.toPeopleDestination() = Article(
    type = ArticleType.Article,
    id = id,
    title = title,
    authorName = author.name,
    authorBio = author.headline,
    avatarSrc = author.avatarUrl,
    excerpt = excerpt,
)

private fun DataHolder.Answer.toPeopleAnswerDisplayItem(): FeedDisplayItem = FeedDisplayItem(
    title = question.title,
    summary = excerpt,
    details = "回答 · $voteupCount 赞同 · $commentCount 评论",
    feed = null,
    navDestinationJson = toPeopleDestination().toFeedDisplayItemNavDestinationJson(),
    raw = this,
)

private fun DataHolder.Article.toPeopleArticleDisplayItem(): FeedDisplayItem = FeedDisplayItem(
    title = title,
    summary = excerpt,
    details = "文章 · $voteupCount 赞同 · $commentCount 评论",
    feed = null,
    navDestinationJson = toPeopleDestination().toFeedDisplayItemNavDestinationJson(),
    raw = this,
)

private fun DataHolder.Pin.toPeoplePinDisplayItem(): FeedDisplayItem? {
    val pinId = id.toLongOrNull() ?: return null
    return FeedDisplayItem(
        title = Ksoup.parse(excerptTitle).text(),
        summary = null,
        details = "想法 · $likeCount 赞 · $commentCount 评论",
        feed = null,
        navDestinationJson = Pin(id = pinId, authorName = author.name).toFeedDisplayItemNavDestinationJson(),
        raw = this,
    )
}

private val PEOPLE_SCREEN_TITLES = listOf(
    "回答",
    "文章",
    "动态",
    "收藏",
    "提问",
    "想法",
    "专栏",
    "粉丝",
    "关注",
    "关注订阅",
)

private val PEOPLE_SCREEN_SUBSCRIPTION_TITLES = listOf(
    "我订阅的专栏",
    "关注的话题",
    "关注的问题",
    "关注的收藏夹",
)

const val PEOPLE_SCREEN_ROOT_TAG = "people_screen_root"
const val PEOPLE_SCREEN_HEADER_TAG = "people_screen_header"
const val PEOPLE_SCREEN_AVATAR_TAG = "people_screen_avatar"
const val PEOPLE_SCREEN_TAB_ROW_TAG = "people_screen_tab_row"
const val PEOPLE_SCREEN_PAGER_TAG = "people_screen_pager"
const val PEOPLE_SCREEN_ANSWERS_LIST_TAG = "people_screen_answers_list"
const val PEOPLE_SCREEN_ARTICLES_LIST_TAG = "people_screen_articles_list"
const val PEOPLE_SCREEN_ACTIVITIES_LIST_TAG = "people_screen_activities_list"
const val PEOPLE_SCREEN_COLLECTIONS_LIST_TAG = "people_screen_collections_list"
const val PEOPLE_SCREEN_QUESTIONS_LIST_TAG = "people_screen_questions_list"
const val PEOPLE_SCREEN_PINS_LIST_TAG = "people_screen_pins_list"
const val PEOPLE_SCREEN_COLUMNS_LIST_TAG = "people_screen_columns_list"
const val PEOPLE_SCREEN_FOLLOWERS_LIST_TAG = "people_screen_followers_list"
const val PEOPLE_SCREEN_FOLLOWING_LIST_TAG = "people_screen_following_list"
const val PEOPLE_SCREEN_SUBSCRIPTION_TABS_TAG = "people_screen_subscription_tabs"
const val PEOPLE_SCREEN_SUBSCRIPTIONS_LIST_TAG = "people_screen_subscriptions_list"
const val PEOPLE_SCREEN_ANSWER_COUNT_TAG = "people_screen_stat_answers"
const val PEOPLE_SCREEN_ARTICLE_COUNT_TAG = "people_screen_stat_articles"
const val PEOPLE_SCREEN_FOLLOWER_COUNT_TAG = "people_screen_stat_followers"
const val PEOPLE_SCREEN_FOLLOWING_COUNT_TAG = "people_screen_stat_following"
const val PEOPLE_SCREEN_FOLLOW_BUTTON_TAG = "people_screen_follow_button"
const val PEOPLE_SCREEN_BLOCK_BUTTON_TAG = "people_screen_block_button"
const val PEOPLE_SCREEN_RECOMMENDATION_BLOCK_BUTTON_TAG = "people_screen_recommendation_block_button"
const val PEOPLE_SCREEN_QUESTION_AUTHOR_BLOCK_BUTTON_TAG = "people_screen_question_author_block_button"
const val PEOPLE_SCREEN_SEARCH_BUTTON_TAG = "people_screen_search_button"
const val PEOPLE_SCREEN_BACK_BUTTON_TAG = "people_screen_back_button"
const val PEOPLE_SCREEN_GITHUB_STARS_TAG = "people_screen_github_stars"
const val PEOPLE_SCREEN_ANSWER_SORT_HOT_TAG = "people_screen_answer_sort_voteups"
const val PEOPLE_SCREEN_ANSWER_SORT_TIME_TAG = "people_screen_answer_sort_created"
const val PEOPLE_SCREEN_ARTICLE_SORT_HOT_TAG = "people_screen_article_sort_voteups"
const val PEOPLE_SCREEN_ARTICLE_SORT_TIME_TAG = "people_screen_article_sort_created"
const val PEOPLE_SCREEN_OFFICIAL_BADGE_TAG = "people_screen_official_badge"

private fun peopleScreenInitialPage(person: Person): Int {
    val jumpToIndex = PEOPLE_SCREEN_TITLES.indexOf(person.jumpTo)
    return if (jumpToIndex >= 0) jumpToIndex else 0
}

data class GithubSocialUiState(
    val title: String,
    val starCount: String,
    val profileUrl: String,
    val iconUrl: String? = null,
)

internal fun DataHolder.People.githubSocialUiState(): GithubSocialUiState? = socialMedias.firstNotNullOfOrNull { media ->
    if (!media.title.startsWith("GitHub", ignoreCase = true)) {
        return@firstNotNullOfOrNull null
    }
    val starCount = media.modules
        .firstOrNull { it.title.equals("stars", ignoreCase = true) }
        ?.value
        ?.takeIf { it.isNotBlank() }
        ?: return@firstNotNullOfOrNull null
    val profileLink = media.link.takeIf { it.isNotBlank() }
        ?: return@firstNotNullOfOrNull null
    val profileUrl = if (profileLink.startsWith("zhihu://", ignoreCase = true)) {
        // link 是知乎内部 AppView，GitHub 用户名来自同一条社交资料的标题。
        val username = media.title
            .substringAfter('·', missingDelimiterValue = "")
            .trim()
            .takeIf { it.isNotBlank() }
            ?: return@firstNotNullOfOrNull null
        "https://github.com/$username"
    } else {
        profileLink
    }

    GithubSocialUiState(
        title = media.title,
        starCount = starCount,
        profileUrl = profileUrl,
        iconUrl = media.icon.takeIf { it.isNotBlank() },
    )
}

/**
 * 用户主页的生产入口。
 *
 * 用户页展示资料头部、关注/屏蔽状态、回答、文章、想法、收藏等内容 tab，并支持从 `Person.jumpTo` 跳到指定子区域。
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PeopleScreen(
    person: Person,
) {
    val navigator = LocalNavigator.current
    val userMessages = rememberUserMessageSink()
    val paginationEnvironment = rememberPaginationEnvironment(allowGuestAccess = false)
    val viewModel = composeViewModel { PersonViewModel(person) }
    val coroutineScope = rememberCoroutineScope()

    val pagerState = rememberPagerState(
        initialPage = peopleScreenInitialPage(person),
        pageCount = { PEOPLE_SCREEN_TITLES.size },
    )
    val readingQueueSourceId = when (pagerState.currentPage) {
        0 -> "people:${person.userTokenOrId}:answers:${viewModel.answersFeedModel.sortBy}"
        1 -> "people:${person.userTokenOrId}:articles:${viewModel.articlesFeedModel.sortBy}"
        2 -> "people:${person.userTokenOrId}:activities:${viewModel.activitiesFeedModel.sort}"
        5 -> "people:${person.userTokenOrId}:pins"
        else -> null
    }
    when (pagerState.currentPage) {
        0 -> RegisterReadingQueueSource(
            sourceId = requireNotNull(readingQueueSourceId),
            items = viewModel.answersFeedModel.allData.map(DataHolder.Answer::toPeopleAnswerDisplayItem),
        )
        1 -> RegisterReadingQueueSource(
            sourceId = requireNotNull(readingQueueSourceId),
            items = viewModel.articlesFeedModel.allData.map(DataHolder.Article::toPeopleArticleDisplayItem),
        )
        2 -> RegisterReadingQueueSource(
            sourceId = requireNotNull(readingQueueSourceId),
            items = viewModel.activitiesFeedModel.displayItems,
        )
        5 -> RegisterReadingQueueSource(
            sourceId = requireNotNull(readingQueueSourceId),
            items = viewModel.pinsFeedModel.allData.mapNotNull(DataHolder.Pin::toPeoplePinDisplayItem),
        )
    }

    // 自己的主页不显示关注/拉黑/屏蔽，这些动作对自己没有意义。
    val selfAccount = rememberAccountSettingsAccountState().value
    val isSelfProfile = selfAccount.login &&
        (
            (person.id.isNotBlank() && person.id == selfAccount.id) ||
                (person.urlToken.isNotBlank() && person.urlToken == selfAccount.urlToken)
        )
    LaunchedEffect(viewModel) {
        try {
            viewModel.load(paginationEnvironment)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            userMessages.showShortMessage("加载用户信息失败: ${e.message}")
        }
    }
    LaunchedEffect(pagerState.currentPage) {
        try {
            viewModel.subFeedModels.getOrNull(pagerState.currentPage)?.let { feedModel ->
                val hasData = when (feedModel) {
                    is BaseFeedViewModel -> feedModel.allData.isNotEmpty() || feedModel.displayItems.isNotEmpty()
                    else -> feedModel.allData.isNotEmpty()
                }
                if (!hasData) {
                    feedModel.loadMore(paginationEnvironment)
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            userMessages.showShortMessage("加载页面内容失败: ${e.message}")
        }
    }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    var expandedHeaderHeightPx by remember { mutableIntStateOf(0) }
    LaunchedEffect(expandedHeaderHeightPx) {
        if (expandedHeaderHeightPx > 0) {
            scrollBehavior.state.heightOffsetLimit = -expandedHeaderHeightPx.toFloat()
        }
    }
    val collapsedFraction = scrollBehavior.state.collapsedFraction
    val onSearch: (() -> Unit)? = if (viewModel.memberHashId.isNotBlank() && viewModel.memberHashId != Person.EMPTY_ID) {
        val memberName = viewModel.name.takeIf { it.isNotBlank() } ?: person.name
        {
            navigator.onNavigate(
                SearchDestination(
                    restrictedMemberHashId = viewModel.memberHashId,
                    restrictedMemberName = memberName,
                ),
            )
        }
    } else {
        null
    }

    Scaffold(
        modifier = Modifier
            .testTag(PEOPLE_SCREEN_ROOT_TAG)
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .fillMaxSize(),
        topBar = {
            Column(
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
            ) {
                PeopleToolRow(
                    onBack = navigator.onNavigateBack,
                    onSearch = onSearch,
                )
                CollapsibleHeader(
                    collapsedFraction = collapsedFraction,
                    onExpandedHeightChanged = { expandedHeaderHeightPx = it },
                ) {
                    UserInfoHeader(
                        viewModel = viewModel,
                        pagerState = pagerState,
                        isSelfProfile = isSelfProfile,
                        modifier = Modifier.testTag(PEOPLE_SCREEN_HEADER_TAG),
                        onFollowToggle = {
                            coroutineScope.launch {
                                try {
                                    viewModel.toggleFollow(paginationEnvironment)
                                } catch (e: Exception) {
                                    userMessages.showShortMessage("操作失败: ${e.message}")
                                }
                            }
                        },
                        onBlockToggle = {
                            coroutineScope.launch {
                                try {
                                    viewModel.toggleBlock(paginationEnvironment)
                                } catch (e: Exception) {
                                    userMessages.showShortMessage("操作失败: ${e.message}")
                                }
                            }
                        },
                        onRecommendationBlockToggle = {
                            coroutineScope.launch {
                                try {
                                    viewModel.toggleRecommendationBlock(paginationEnvironment)
                                    userMessages.showShortMessage(if (viewModel.isBlockedInRecommendations) "已屏蔽推荐" else "已取消屏蔽推荐")
                                } catch (e: Exception) {
                                    userMessages.showShortMessage("操作失败: ${e.message}")
                                }
                            }
                        },
                        onQuestionAuthorBlockToggle = {
                            coroutineScope.launch {
                                try {
                                    viewModel.toggleQuestionAuthorBlock(paginationEnvironment)
                                    userMessages.showShortMessage(if (viewModel.isBlockedAsQuestionAuthor) "已屏蔽其提问" else "已取消屏蔽其提问")
                                } catch (e: Exception) {
                                    userMessages.showShortMessage("操作失败: ${e.message}")
                                }
                            }
                        },
                    )
                }
                PeopleTabRow(
                    pagerState = pagerState,
                    onSelect = { index ->
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding),
        ) {
            HorizontalPager(
                state = pagerState,
                // 相邻页也保留组合，切 Tab 再切回不会丢掉 LazyListState 滚动位置。
                beyondViewportPageCount = 2,
                modifier = Modifier
                    .weight(1f)
                    .testTag(PEOPLE_SCREEN_PAGER_TAG),
            ) { page ->
                val listState = rememberLazyListState()
                val pageTurnTarget = rememberPageTurnTarget(
                    listState = listState,
                    enabled = pagerState.currentPage == page,
                )
                when (page) {
                    0 -> {
                        // 回答
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("people_screen_page_$page"),
                        ) {
                            SortBar(
                                currentSort = viewModel.answersFeedModel.sortBy,
                                onSortChange = { viewModel.answersFeedModel.changeSortBy(it, paginationEnvironment) },
                                hotTag = PEOPLE_SCREEN_ANSWER_SORT_HOT_TAG,
                                timeTag = PEOPLE_SCREEN_ANSWER_SORT_TIME_TAG,
                            )
                            PaginatedList(
                                items = viewModel.answersFeedModel.allData,
                                listState = listState,
                                onLoadMore = { viewModel.answersFeedModel.loadMore(paginationEnvironment) },
                                isEnd = { viewModel.answersFeedModel.isEnd },
                                footer = ProgressIndicatorFooter,
                                contentPadding = PaddingValues(
                                    start = ZhihuSpacing.ExtraSmall,
                                    end = ZhihuSpacing.ExtraSmall,
                                    top = ZhihuSpacing.ExtraSmall,
                                    bottom = ZhihuSpacing.Medium,
                                ),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .pageTurnViewportWithGuide(pageTurnTarget)
                                    .testTag(PEOPLE_SCREEN_ANSWERS_LIST_TAG),
                                key = { it.id },
                            ) { answer ->
                                PeopleAnswerCard(
                                    answer = answer,
                                    readingQueueSourceId = readingQueueSourceId,
                                    itemTag = "people_screen_answer_item_${answer.id}",
                                )
                            }
                        }
                    }

                    1 -> {
                        // 文章
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("people_screen_page_$page"),
                        ) {
                            SortBar(
                                currentSort = viewModel.articlesFeedModel.sortBy,
                                onSortChange = { viewModel.articlesFeedModel.changeSortBy(it, paginationEnvironment) },
                                hotTag = PEOPLE_SCREEN_ARTICLE_SORT_HOT_TAG,
                                timeTag = PEOPLE_SCREEN_ARTICLE_SORT_TIME_TAG,
                            )
                            PaginatedList(
                                items = viewModel.articlesFeedModel.allData,
                                listState = listState,
                                onLoadMore = { viewModel.articlesFeedModel.loadMore(paginationEnvironment) },
                                isEnd = { viewModel.articlesFeedModel.isEnd },
                                footer = ProgressIndicatorFooter,
                                contentPadding = PaddingValues(
                                    start = ZhihuSpacing.ExtraSmall,
                                    end = ZhihuSpacing.ExtraSmall,
                                    top = ZhihuSpacing.ExtraSmall,
                                    bottom = ZhihuSpacing.Medium,
                                ),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .pageTurnViewportWithGuide(pageTurnTarget)
                                    .testTag(PEOPLE_SCREEN_ARTICLES_LIST_TAG),
                                key = { it.id },
                            ) { article ->
                                PeopleArticleCard(
                                    article = article,
                                    readingQueueSourceId = readingQueueSourceId,
                                    itemTag = "people_screen_article_item_${article.id}",
                                )
                            }
                        }
                    }

                    2 -> {
                        // 动态
                        PaginatedList(
                            items = viewModel.activitiesFeedModel.displayItems,
                            listState = listState,
                            onLoadMore = { viewModel.activitiesFeedModel.loadMore(paginationEnvironment) },
                            isEnd = { viewModel.activitiesFeedModel.isEnd },
                            footer = ProgressIndicatorFooter,
                            modifier = Modifier
                                .fillMaxSize()
                                .pageTurnViewportWithGuide(pageTurnTarget)
                                .testTag(PEOPLE_SCREEN_ACTIVITIES_LIST_TAG),
                        ) {
                            FeedCard(
                                it,
                                readingQueueSourceId = readingQueueSourceId,
                                modifier = Modifier.testTag("people_screen_activity_item_${it.stableKey}"),
                                horizontalPadding = 4.dp,
                            )
                        }
                    }

                    3 -> {
                        // 收藏
                        PaginatedList(
                            items = viewModel.collectionsFeedModel.allData,
                            listState = listState,
                            onLoadMore = { viewModel.collectionsFeedModel.loadMore(paginationEnvironment) },
                            isEnd = { viewModel.collectionsFeedModel.isEnd },
                            footer = ProgressIndicatorFooter,
                            modifier = Modifier
                                .fillMaxSize()
                                .pageTurnViewportWithGuide(pageTurnTarget)
                                .testTag(PEOPLE_SCREEN_COLLECTIONS_LIST_TAG),
                            key = { it.id },
                        ) { collection ->
                            CollectionListItem(
                                collection = collection,
                                itemTag = "people_screen_collection_item_${collection.id}",
                            )
                        }
                    }

                    4 -> {
                        // 提问
                        PaginatedList(
                            items = viewModel.questionsFeedModel.allData,
                            listState = listState,
                            onLoadMore = { viewModel.questionsFeedModel.loadMore(paginationEnvironment) },
                            isEnd = { viewModel.questionsFeedModel.isEnd },
                            footer = ProgressIndicatorFooter,
                            modifier = Modifier
                                .fillMaxSize()
                                .pageTurnViewportWithGuide(pageTurnTarget)
                                .testTag(PEOPLE_SCREEN_QUESTIONS_LIST_TAG),
                            key = { it.id },
                        ) { question ->
                            QuestionListItem(
                                question = question,
                                itemTag = "people_screen_question_item_${question.id}",
                            )
                        }
                    }

                    5 -> {
                        // 想法
                        PaginatedList(
                            items = viewModel.pinsFeedModel.allData,
                            listState = listState,
                            onLoadMore = { viewModel.pinsFeedModel.loadMore(paginationEnvironment) },
                            isEnd = { viewModel.pinsFeedModel.isEnd },
                            footer = ProgressIndicatorFooter,
                            modifier = Modifier
                                .fillMaxSize()
                                .pageTurnViewportWithGuide(pageTurnTarget)
                                .testTag(PEOPLE_SCREEN_PINS_LIST_TAG),
                            key = { it.id },
                        ) { pin ->
                            PinListItem(
                                pin = pin,
                                itemTag = "people_screen_pin_item_${pin.id}",
                                readingQueueSourceId = readingQueueSourceId,
                            )
                        }
                    }

                    6 -> {
                        // 专栏
                        PaginatedList(
                            items = viewModel.columnsFeedModel.allData,
                            listState = listState,
                            onLoadMore = { viewModel.columnsFeedModel.loadMore(paginationEnvironment) },
                            isEnd = { viewModel.columnsFeedModel.isEnd },
                            footer = ProgressIndicatorFooter,
                            modifier = Modifier
                                .fillMaxSize()
                                .pageTurnViewportWithGuide(pageTurnTarget)
                                .testTag(PEOPLE_SCREEN_COLUMNS_LIST_TAG),
                            key = { it.id },
                        ) { column ->
                            ColumnListItem(
                                column = column,
                                itemTag = "people_screen_column_item_${column.id}",
                            )
                        }
                    }

                    7 -> {
                        // 粉丝
                        PaginatedList(
                            items = viewModel.followersFeedModel.allData,
                            listState = listState,
                            onLoadMore = { viewModel.followersFeedModel.loadMore(paginationEnvironment) },
                            isEnd = { viewModel.followersFeedModel.isEnd },
                            footer = ProgressIndicatorFooter,
                            modifier = Modifier
                                .fillMaxSize()
                                .pageTurnViewportWithGuide(pageTurnTarget)
                                .testTag(PEOPLE_SCREEN_FOLLOWERS_LIST_TAG),
                            key = { it.id },
                        ) { people ->
                            PeopleListItem(
                                people = people,
                                itemTag = "people_screen_follower_item_${people.id}",
                                actionTag = "people_screen_follower_action_${people.id}",
                            )
                        }
                    }

                    8 -> {
                        // 关注
                        PaginatedList(
                            items = viewModel.followingFeedModel.allData,
                            listState = listState,
                            onLoadMore = { viewModel.followingFeedModel.loadMore(paginationEnvironment) },
                            isEnd = { viewModel.followingFeedModel.isEnd },
                            footer = ProgressIndicatorFooter,
                            modifier = Modifier
                                .fillMaxSize()
                                .pageTurnViewportWithGuide(pageTurnTarget)
                                .testTag(PEOPLE_SCREEN_FOLLOWING_LIST_TAG),
                            key = { it.id },
                        ) { people ->
                            PeopleListItem(
                                people = people,
                                itemTag = "people_screen_following_item_${people.id}",
                                actionTag = "people_screen_following_action_${people.id}",
                            )
                        }
                    }

                    9 -> {
                        FollowingSubscriptionsPage(
                            viewModel = viewModel,
                            listState = listState,
                            pageTurnTarget = pageTurnTarget,
                            onLoadMore = { subscriptionPage ->
                                when (subscriptionPage) {
                                    0 -> viewModel.followingColumnsFeedModel.loadMore(paginationEnvironment)
                                    1 -> viewModel.followingTopicsFeedModel.loadMore(paginationEnvironment)
                                    2 -> viewModel.followingQuestionsFeedModel.loadMore(paginationEnvironment)
                                    3 -> viewModel.followingCollectionsFeedModel.loadMore(paginationEnvironment)
                                }
                            },
                            modifier = Modifier.testTag("people_screen_page_$page"),
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FollowingSubscriptionsPage(
    viewModel: PersonViewModel,
    onLoadMore: (Int) -> Unit,
    listState: LazyListState,
    pageTurnTarget: PageTurnTarget,
    modifier: Modifier = Modifier,
) {
    var selectedPage by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(selectedPage) {
        onLoadMore(selectedPage)
    }

    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .testTag(PEOPLE_SCREEN_SUBSCRIPTION_TABS_TAG)
                .padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PEOPLE_SCREEN_SUBSCRIPTION_TITLES.forEachIndexed { index, title ->
                OutlinedButton(
                    onClick = { selectedPage = index },
                    modifier = Modifier.testTag("people_screen_subscription_tab_$index"),
                    shape = ZhihuShapeTokens.Badge,
                    colors = if (selectedPage == index) {
                        ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    } else {
                        ButtonDefaults.outlinedButtonColors()
                    },
                ) {
                    Text(title)
                }
            }
        }

        when (selectedPage) {
            0 -> PaginatedList(
                items = viewModel.followingColumnsFeedModel.allData,
                listState = listState,
                onLoadMore = { onLoadMore(0) },
                isEnd = { viewModel.followingColumnsFeedModel.isEnd },
                footer = ProgressIndicatorFooter,
                modifier = Modifier
                    .fillMaxSize()
                    .pageTurnViewportWithGuide(pageTurnTarget)
                    .testTag(PEOPLE_SCREEN_SUBSCRIPTIONS_LIST_TAG),
                key = { it.id },
            ) { column ->
                ColumnListItem(
                    column = column,
                    itemTag = "people_screen_column_item_${column.id}",
                )
            }

            1 -> PaginatedList(
                items = viewModel.followingTopicsFeedModel.allData,
                listState = listState,
                onLoadMore = { onLoadMore(1) },
                isEnd = { viewModel.followingTopicsFeedModel.isEnd },
                footer = ProgressIndicatorFooter,
                modifier = Modifier
                    .fillMaxSize()
                    .pageTurnViewportWithGuide(pageTurnTarget)
                    .testTag(PEOPLE_SCREEN_SUBSCRIPTIONS_LIST_TAG),
                key = { it.displayId },
            ) { topic ->
                FollowedTopicListItem(topic)
            }

            2 -> PaginatedList(
                items = viewModel.followingQuestionsFeedModel.allData,
                listState = listState,
                onLoadMore = { onLoadMore(2) },
                isEnd = { viewModel.followingQuestionsFeedModel.isEnd },
                footer = ProgressIndicatorFooter,
                modifier = Modifier
                    .fillMaxSize()
                    .pageTurnViewportWithGuide(pageTurnTarget)
                    .testTag(PEOPLE_SCREEN_SUBSCRIPTIONS_LIST_TAG),
                key = { it.id },
            ) { question ->
                FollowedQuestionListItem(question)
            }

            3 -> PaginatedList(
                items = viewModel.followingCollectionsFeedModel.allData,
                listState = listState,
                onLoadMore = { onLoadMore(3) },
                isEnd = { viewModel.followingCollectionsFeedModel.isEnd },
                footer = ProgressIndicatorFooter,
                modifier = Modifier
                    .fillMaxSize()
                    .pageTurnViewportWithGuide(pageTurnTarget)
                    .testTag(PEOPLE_SCREEN_SUBSCRIPTIONS_LIST_TAG),
                key = { it.id },
            ) { collection ->
                CollectionListItem(
                    collection = collection,
                    itemTag = "people_screen_collection_item_${collection.id}",
                )
            }
        }
    }
}

@Composable
private fun CollectionListItem(
    collection: DataHolder.Collection,
    itemTag: String? = null,
) {
    val navigator = LocalNavigator.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (itemTag != null) Modifier.testTag(itemTag) else Modifier)
            .clickable {
                navigator.onNavigate(CollectionContent(collection.id))
            }.padding(vertical = 8.dp, horizontal = 4.dp),
    ) {
        Text(
            text = collection.title,
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = "${collection.answerCount} 内容 · ${collection.followerCount} 关注",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
private fun QuestionListItem(
    question: DataHolder.Question,
    itemTag: String? = null,
) {
    val navigator = LocalNavigator.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (itemTag != null) Modifier.testTag(itemTag) else Modifier)
            .clickable {
                navigator.onNavigate(Question(question.id, question.title))
            }.padding(vertical = 8.dp, horizontal = 4.dp),
    ) {
        Text(
            text = question.title,
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = "${question.answerCount} 回答 · ${question.followerCount} 关注",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
private fun PinListItem(
    pin: DataHolder.Pin,
    itemTag: String? = null,
    readingQueueSourceId: String? = null,
) {
    val navigator = LocalNavigator.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (itemTag != null) Modifier.testTag(itemTag) else Modifier)
            .clickable {
                navigator.onNavigate(
                    Pin(
                        id = pin.id.toLong(),
                        authorName = pin.author.name,
                        readingQueueSourceId = readingQueueSourceId,
                    ),
                )
            }.padding(vertical = 8.dp, horizontal = 4.dp),
    ) {
        val text = remember { Ksoup.parse(pin.excerptTitle).text() }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = "${pin.likeCount} 赞 · ${pin.commentCount} 评论",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
private fun ColumnListItem(
    column: DataHolder.Column,
    itemTag: String? = null,
) {
    val openZhihuWebUrl = rememberZhihuWebUrlOpener()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (itemTag != null) Modifier.testTag(itemTag) else Modifier)
            .clickable {
                openZhihuWebUrl(column.webUrl())
            }.padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = column.title,
                style = MaterialTheme.typography.titleMedium,
            )
            if (column.description.isNotEmpty()) {
                Text(
                    text = column.description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            Text(
                text = "${column.articlesCount} 文章 · ${column.followerCount.coerceAtLeast(column.followers)} 关注",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun FollowedQuestionListItem(question: FollowedQuestion) {
    val navigator = LocalNavigator.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("people_screen_followed_question_item_${question.id}")
            .clickable {
                question.id.toLongOrNull()?.let {
                    navigator.onNavigate(Question(it, question.title))
                }
            }.padding(vertical = 8.dp, horizontal = 4.dp),
    ) {
        Text(
            text = question.title,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
private fun FollowedTopicListItem(topic: FollowedTopic) {
    val navigator = LocalNavigator.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("people_screen_followed_topic_item_${topic.displayId}")
            .clickable {
                navigator.onNavigate(
                    com.github.zly2006.zhihu.navigation
                        .Topic(topic.displayId, topic.displayName),
                )
            }.padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = topic.displayAvatarUrl,
            contentDescription = "话题头像",
            modifier = Modifier
                .padding(end = 12.dp)
                .size(40.dp)
                .clip(CircleShape),
        )
        Text(
            text = topic.displayName,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f),
        )
    }
}

private fun DataHolder.Column.webUrl(): String = when {
    url.contains("/api/v4/columns/") ->
        url
            .replace("http://", "https://")
            .replace("/api/v4/columns/", "/column/")

    url.startsWith("http") && !url.contains("/api/") -> url.replace("http://", "https://")
    else -> "https://www.zhihu.com/column/$id"
}

@Composable
private fun PeopleListItem(
    people: DataHolder.People,
    itemTag: String? = null,
    actionTag: String? = null,
) {
    val navigator = LocalNavigator.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (itemTag != null) Modifier.testTag(itemTag) else Modifier)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = people.avatarUrl,
            contentDescription = null,
            modifier = Modifier
                .padding(end = 12.dp)
                .size(48.dp)
                .clip(CircleShape),
        )
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = people.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                val officialBadge = people.badgeV2.officialBadge()
                if (officialBadge?.isUsefulInList == true) {
                    AuthorBadge(
                        badge = officialBadge,
                        compact = true,
                        modifier = Modifier.padding(start = 6.dp),
                    )
                }
            }
            if (people.headline.isNotEmpty()) {
                Text(
                    text = people.headline,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(top = 4.dp),
            ) {
                Text(
                    text = "${people.answerCount} 回答",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "${people.articlesCount} 文章",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "${people.followerCount} 粉丝",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        OutlinedButton(
            onClick = {
                navigator.onNavigate(
                    Person(
                        id = people.id,
                        name = people.name,
                        urlToken = people.urlToken ?: "",
                    ),
                )
            },
            modifier = if (actionTag != null) Modifier.testTag(actionTag) else Modifier,
        ) {
            Text("查看")
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: Int,
    modifier: Modifier = Modifier,
    tag: String? = null,
    onClick: () -> Unit,
) {
    val colors = contentFirstColors
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .then(if (tag != null) Modifier.testTag(tag) else Modifier)
            .clip(ZhihuShapeTokens.Card)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = ZhihuSpacing.ExtraSmall),
    ) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = colors.primaryText,
            maxLines = 1,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = colors.tertiaryText,
            maxLines = 1,
        )
    }
}

@Composable
private fun OfficialBadgeDetails(
    badges: List<OfficialBadge>,
    modifier: Modifier = Modifier,
) {
    if (badges.isEmpty()) return
    val colors = contentFirstColors
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        badges.forEach { badge ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (badge.iconUrl.isNotBlank()) {
                    if (badge.iconUrl == DataHolder.ZH_PLUS_AUTHOR_BADGE_ICON) {
                        Image(
                            painter = painterResource(Res.drawable.ic_zh_plus_author_badge),
                            contentDescription = badge.description,
                            modifier = Modifier
                                .padding(end = ZhihuSpacing.ExtraSmall)
                                .size(16.dp),
                        )
                    } else {
                        AsyncImage(
                            model = badge.iconUrl,
                            contentDescription = badge.description,
                            modifier = Modifier
                                .padding(end = ZhihuSpacing.ExtraSmall)
                                .size(16.dp),
                        )
                    }
                }
                Text(
                    text = "${badge.peopleDetailTitle}: ${badge.description}",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.tertiaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

private val OfficialBadge.peopleDetailTitle: String
    get() = when {
        title == "认证" || title == "已认证的个人" -> "认证信息"
        else -> title
    }

@Composable
private fun SortBar(
    currentSort: String,
    onSortChange: (String) -> Unit,
    hotTag: String? = null,
    timeTag: String? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ZhihuSpacing.ExtraSmall)
            .padding(top = ZhihuSpacing.Small, bottom = ZhihuSpacing.ExtraSmall),
        horizontalArrangement = Arrangement.spacedBy(ZhihuSpacing.Small),
    ) {
        SortChip(
            label = "按热度",
            selected = currentSort == "voteups",
            tag = hotTag,
            onClick = { onSortChange("voteups") },
        )
        SortChip(
            label = "按时间",
            selected = currentSort == "created",
            tag = timeTag,
            onClick = { onSortChange("created") },
        )
    }
}

@Composable
private fun SortChip(
    label: String,
    selected: Boolean,
    tag: String?,
    onClick: () -> Unit,
) {
    val colors = contentFirstColors
    Box(
        modifier = Modifier
            .then(if (tag != null) Modifier.testTag(tag) else Modifier)
            .clip(ZhihuShapeTokens.Pill)
            .background(if (selected) colors.accent.copy(alpha = 0.12f) else colors.functionalSurface)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = ZhihuSpacing.Medium, vertical = ZhihuSpacing.ExtraSmall),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) colors.accent else colors.secondaryText,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
private fun CollapsibleHeader(
    collapsedFraction: Float,
    onExpandedHeightChanged: (Int) -> Unit,
    content: @Composable () -> Unit,
) {
    SubcomposeLayout(modifier = Modifier.clipToBounds()) { constraints ->
        val placeable = subcompose("header", content).single().measure(
            constraints.copy(minHeight = 0, maxHeight = Constraints.Infinity),
        )
        if (placeable.height > 0) {
            onExpandedHeightChanged(placeable.height)
        }
        // 资料区随折叠整体上移而不是从底部裁切：顶部资料先滑出，底部操作区最后消失，
        // 视觉上等同于跟随列表一起滚走，收起态不会残留头像/昵称。
        val visibleHeight = (placeable.height * (1f - collapsedFraction))
            .coerceIn(0f, placeable.height.toFloat())
        val height = visibleHeight.roundToInt()
        layout(constraints.maxWidth, height) {
            placeable.place(0, (visibleHeight - placeable.height).roundToInt())
        }
    }
}

@Composable
private fun PeopleToolRow(
    onBack: () -> Unit,
    onSearch: (() -> Unit)?,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .padding(horizontal = ZhihuSpacing.Small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ToolCircleButton(
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "返回",
            tag = PEOPLE_SCREEN_BACK_BUTTON_TAG,
            onClick = onBack,
        )
        Spacer(Modifier.weight(1f))
        if (onSearch != null) {
            ToolCircleButton(
                icon = Icons.Default.Search,
                contentDescription = "搜索 TA 的创作",
                tag = PEOPLE_SCREEN_SEARCH_BUTTON_TAG,
                onClick = onSearch,
            )
        }
    }
}

@Composable
private fun ToolCircleButton(
    icon: ImageVector,
    contentDescription: String,
    tag: String,
    onClick: () -> Unit,
) {
    val colors = contentFirstColors
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(colors.functionalSurface)
            .clickable(role = Role.Button, onClick = onClick)
            .testTag(tag),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = colors.primaryText,
            modifier = Modifier.size(ZhihuSizes.IconSmall),
        )
    }
}

@Composable
private fun PeopleTabRow(
    pagerState: PagerState,
    onSelect: (Int) -> Unit,
) {
    val colors = contentFirstColors
    val tabListState = rememberLazyListState()
    LaunchedEffect(pagerState.currentPage) {
        tabListState.animateScrollToItem(pagerState.currentPage)
    }
    LazyRow(
        state = tabListState,
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .testTag(PEOPLE_SCREEN_TAB_ROW_TAG),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        itemsIndexed(PEOPLE_SCREEN_TITLES) { index, title ->
            val selected = pagerState.currentPage == index
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .clip(ZhihuShapeTokens.Section)
                    .clickable(role = Role.Tab) { onSelect(index) }
                    .testTag("people_screen_tab_$index")
                    .semantics { this.selected = selected }
                    .padding(horizontal = ZhihuSpacing.Small),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = title,
                    style = ZhihuTypography.FeedAuthorName.copy(
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    ),
                    color = if (selected) colors.primaryText else colors.tertiaryText,
                    maxLines = 1,
                )
                if (selected) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 2.dp)
                            .size(width = 24.dp, height = 3.dp)
                            .clip(ZhihuShapeTokens.Badge)
                            .background(colors.accent),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun UserInfoHeader(
    viewModel: PersonViewModel,
    pagerState: PagerState,
    isSelfProfile: Boolean,
    modifier: Modifier = Modifier,
    onFollowToggle: () -> Unit,
    onBlockToggle: () -> Unit,
    onRecommendationBlockToggle: () -> Unit,
    onQuestionAuthorBlockToggle: () -> Unit,
) {
    val colors = contentFirstColors
    val coroutineScope = rememberCoroutineScope()
    val openImagePreview = rememberImagePreviewOpener()
    val openExternalUrl = rememberExternalUrlOpener()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = ZhihuSpacing.Medium)
            .padding(top = ZhihuSpacing.ExtraSmall, bottom = ZhihuSpacing.Medium),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = viewModel.avatar,
                contentDescription = "查看头像大图",
                modifier = Modifier
                    .testTag(PEOPLE_SCREEN_AVATAR_TAG)
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(colors.functionalSurface)
                    .clickable {
                        openImagePreview(viewModel.avatar.substringBefore("_") + ".jpg")
                    },
            )
            Spacer(Modifier.width(ZhihuSpacing.Medium))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = viewModel.name,
                        style = ZhihuTypography.ContentTitle,
                        color = colors.primaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    viewModel.officialBadge?.let { badge ->
                        AuthorBadge(
                            badge = badge,
                            modifier = Modifier
                                .padding(start = ZhihuSpacing.ExtraSmall)
                                .testTag(PEOPLE_SCREEN_OFFICIAL_BADGE_TAG),
                        )
                    }
                }
                if (viewModel.headline.isNotEmpty()) {
                    Text(
                        text = viewModel.headline,
                        style = ZhihuTypography.ContentSignature,
                        color = colors.tertiaryText,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
        }
        OfficialBadgeDetails(
            badges = viewModel.officialBadgeDetails,
            modifier = Modifier.padding(top = ZhihuSpacing.Small),
        )
        viewModel.githubSocial?.let { githubSocial ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = ZhihuSpacing.ExtraSmall)
                    .clip(ZhihuShapeTokens.Section)
                    .clickable { openExternalUrl(githubSocial.profileUrl) }
                    .testTag(PEOPLE_SCREEN_GITHUB_STARS_TAG)
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(ZhihuSpacing.ExtraSmall),
            ) {
                githubSocial.iconUrl?.let { iconUrl ->
                    AsyncImage(
                        model = iconUrl,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                    )
                }
                Text(
                    text = githubSocial.title,
                    style = ZhihuTypography.FeedMeta,
                    color = colors.secondaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "· ${githubSocial.starCount} stars",
                    style = ZhihuTypography.FeedMeta,
                    color = colors.tertiaryText,
                    maxLines = 1,
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = ZhihuSpacing.Medium),
        ) {
            StatItem(
                label = "回答",
                value = viewModel.answerCount,
                modifier = Modifier.weight(1f),
                tag = PEOPLE_SCREEN_ANSWER_COUNT_TAG,
            ) {
                coroutineScope.launch { pagerState.animateScrollToPage(0) }
            }
            StatItem(
                label = "文章",
                value = viewModel.articleCount,
                modifier = Modifier.weight(1f),
                tag = PEOPLE_SCREEN_ARTICLE_COUNT_TAG,
            ) {
                coroutineScope.launch { pagerState.animateScrollToPage(1) }
            }
            StatItem(
                label = "粉丝",
                value = viewModel.followerCount,
                modifier = Modifier.weight(1f),
                tag = PEOPLE_SCREEN_FOLLOWER_COUNT_TAG,
            ) {
                coroutineScope.launch { pagerState.animateScrollToPage(7) }
            }
            StatItem(
                label = "关注",
                value = viewModel.followingCount,
                modifier = Modifier.weight(1f),
                tag = PEOPLE_SCREEN_FOLLOWING_COUNT_TAG,
            ) {
                coroutineScope.launch { pagerState.animateScrollToPage(8) }
            }
        }
        if (!isSelfProfile) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = ZhihuSpacing.Medium),
                horizontalArrangement = Arrangement.spacedBy(ZhihuSpacing.Small),
                verticalArrangement = Arrangement.spacedBy(ZhihuSpacing.Small),
            ) {
                FollowPill(isFollowing = viewModel.isFollowing, onClick = onFollowToggle)
                ActionChip(
                    text = if (viewModel.isBlocking) "取消拉黑" else "拉黑",
                    tag = PEOPLE_SCREEN_BLOCK_BUTTON_TAG,
                    onClick = onBlockToggle,
                )
                ActionChip(
                    text = if (viewModel.isBlockedInRecommendations) "取消屏蔽推荐" else "屏蔽推荐",
                    tag = PEOPLE_SCREEN_RECOMMENDATION_BLOCK_BUTTON_TAG,
                    onClick = onRecommendationBlockToggle,
                )
                ActionChip(
                    text = if (viewModel.isBlockedAsQuestionAuthor) "取消屏蔽其提问" else "屏蔽其提问",
                    tag = PEOPLE_SCREEN_QUESTION_AUTHOR_BLOCK_BUTTON_TAG,
                    onClick = onQuestionAuthorBlockToggle,
                )
            }
        }
    }
}

@Composable
private fun FollowPill(
    isFollowing: Boolean,
    onClick: () -> Unit,
) {
    val colors = contentFirstColors
    Box(
        modifier = Modifier
            .height(36.dp)
            .clip(ZhihuShapeTokens.Pill)
            .background(if (isFollowing) colors.functionalSurface else colors.accentFill)
            .clickable(role = Role.Button, onClick = onClick)
            .testTag(PEOPLE_SCREEN_FOLLOW_BUTTON_TAG)
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (isFollowing) "取消关注" else "关注",
            style = MaterialTheme.typography.labelLarge,
            color = if (isFollowing) colors.secondaryText else MaterialTheme.colorScheme.onPrimary,
        )
    }
}

@Composable
private fun ActionChip(
    text: String,
    tag: String,
    onClick: () -> Unit,
) {
    val colors = contentFirstColors
    Box(
        modifier = Modifier.height(36.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .height(30.dp)
                .clip(ZhihuShapeTokens.Pill)
                .border(1.dp, colors.divider, ZhihuShapeTokens.Pill)
                .clickable(role = Role.Button, onClick = onClick)
                .testTag(tag)
                .padding(horizontal = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = colors.secondaryText,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun PeopleAnswerCard(
    answer: DataHolder.Answer,
    readingQueueSourceId: String?,
    itemTag: String,
) {
    val navigator = LocalNavigator.current
    PeopleCompactCard(
        title = answer.question.title,
        titleStyle = ZhihuTypography.FeedSummary.copy(fontWeight = FontWeight.SemiBold),
        summary = answer.excerpt,
        meta = answer.peopleMetaText(),
        thumbnailUrl = null,
        itemTag = itemTag,
        onClick = {
            navigator.onNavigate(
                answer.toPeopleDestination().withReadingQueueSource(readingQueueSourceId),
            )
        },
    )
}

@Composable
private fun PeopleArticleCard(
    article: DataHolder.Article,
    readingQueueSourceId: String?,
    itemTag: String,
) {
    val navigator = LocalNavigator.current
    PeopleCompactCard(
        title = article.title,
        titleStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        summary = article.excerpt,
        meta = article.peopleMetaText(),
        thumbnailUrl = article.coverThumbnailUrl(),
        itemTag = itemTag,
        onClick = {
            navigator.onNavigate(
                article.toPeopleDestination().withReadingQueueSource(readingQueueSourceId),
            )
        },
    )
}

@Composable
private fun PeopleCompactCard(
    title: String,
    titleStyle: TextStyle,
    summary: String?,
    meta: String,
    thumbnailUrl: String?,
    itemTag: String,
    onClick: () -> Unit,
) {
    val colors = contentFirstColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = ZhihuSpacing.Small)
            .testTag(itemTag)
            .padding(horizontal = ZhihuSpacing.ScreenGutter)
            .clip(ZhihuShapeTokens.Card)
            .background(colors.functionalSurface)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = ZhihuSpacing.Large)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    style = titleStyle,
                    color = colors.primaryText,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                PeopleCardMenu()
            }
            if (!summary.isNullOrBlank()) {
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.secondaryText,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
            Text(
                text = meta,
                style = ZhihuTypography.ActionCount,
                color = colors.tertiaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = ZhihuSpacing.Small),
            )
        }
        if (thumbnailUrl != null) {
            Spacer(Modifier.width(ZhihuSpacing.Medium))
            AsyncImage(
                model = thumbnailUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(width = 96.dp, height = 72.dp)
                    .clip(ZhihuShapeTokens.Thumbnail),
            )
        }
    }
}

@Composable
private fun PeopleCardMenu() {
    val colors = contentFirstColors
    val navigator = LocalNavigator.current
    var expanded by remember { mutableStateOf(false) }
    Box {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .clickable(role = Role.Button) { expanded = true }
                .semantics { contentDescription = "更多选项" },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = null,
                tint = colors.tertiaryText,
                modifier = Modifier.size(18.dp),
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text("外观设置") },
                onClick = {
                    expanded = false
                    navigator.onNavigate(Account.AppearanceSettings())
                },
            )
        }
    }
}

private fun compactCountText(count: Int): String = formatCompactCount(count).replace(" ", "")

private fun DataHolder.Answer.peopleMetaText(): String =
    "${compactCountText(voteupCount)} 赞同 · ${compactCountText(commentCount)} 评论 · ${formatRelativeTime(createdTime)}"

private fun DataHolder.Article.peopleMetaText(): String =
    "${compactCountText(voteupCount)} 赞同 · ${compactCountText(commentCount)} 评论 · ${formatRelativeTime(created)}"

/** 文章列表的封面字段当前只映射到 `thumbnailInfo`，没有该字段时不显示缩略图。 */
private fun DataHolder.Article.coverThumbnailUrl(): String? =
    thumbnailInfo
        ?.thumbnails
        ?.firstOrNull()
        ?.let { thumbnail ->
            when (thumbnail) {
                is DataHolder.ThumbnailRich -> thumbnail.url
                is DataHolder.ThumbnailString -> thumbnail.value
                is DataHolder.ThumbnailVideo -> thumbnail.url
            }
        }?.takeIf { it.isNotBlank() }
