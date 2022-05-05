package com.stfalcon.sample.common.models

import com.stfalcon.imageviewer.common.pager.RecyclingPagerAdapter

object Demo {

//    private const val POSTERS_PATH = "https://raw.githubusercontent.com/stfalcon-studio/StfalconImageViewer/master/images/posters"
    private const val POSTERS_PATH = "https://tenfei01.cfp.cn/creative/vcg/nowater800/new"
    private const val MISC_PATH = "https://raw.githubusercontent.com/stfalcon-studio/StfalconImageViewer/master/images/misc"


    val posters = listOf(

//        Poster(url = "$POSTERS_PATH/VCG41N1278720325.jpg", description = "Vincent Vega is a hitman and associate of Marsellus Wallace. He had a brother named Vic Vega who was shot and killed by an undercover cop while on a job. He worked in Amsterdam for over three years and recently returned to Los Angeles, where he has been partnered with Jules Winnfield.",imageType = 1),
        Poster(url = "$POSTERS_PATH/VCG41N1272819263.jpg", description = "Jules Winnfield - initially he is a Hitman working alongside Vincent Vega but after revelation, or as he refers to it \"a moment of clarity\" he decides to leave to \"Walk the Earth.\" During the film he is stated to be from Inglewood, California",viewType = RecyclingPagerAdapter.VIEW_TYPE_IMAGE),
        Poster(url = "$POSTERS_PATH/VCG211283987703.jpg", description = "Korben Dallas. A post-America taxi driver in New York City with a grand military background simply lives his life day to day, that is, before he meets Leeloo. Leeloo captures his heart soon after crashing into his taxi cab one day after escaping from a government-run laboratory. Korben soon finds himself running from the authorities in order to protect Leeloo, as well as becoming the center of a desperate ploy to save the world from an unknown evil.",viewType = RecyclingPagerAdapter.VIEW_TYPE_TEXT),
        Poster(url = "$POSTERS_PATH/VCG211283708958.jpg", description = "Dominic \"Dom\" Toretto is the brother of Mia Toretto, uncle to Jack and husband to Letty Ortiz. The protagonist in The Fast and the Furious franchise, Dominic is an elite street racer and auto mechanic.",viewType = RecyclingPagerAdapter.VIEW_TYPE_IMAGE),
        Poster(url = "$POSTERS_PATH/VCG211298406768.jpg", description = "Martin Seamus \"Marty\" McFly Sr. - he is the world's second time traveler, the first to travel backwards in time and the first human to travel though time. He was also a high school student at Hill Valley High School in 1985. He is best friends with Dr. Emmett Brown, who unveiled his first working invention to him.",viewType = RecyclingPagerAdapter.VIEW_TYPE_IMAGE),
        Poster(url = "https://img.zcool.cn/community/013f995ade9eb7a801214a6187d430.jpg?x-oss-process=image/auto-orient,1/resize,m_lfit,w_1280,limit_1/sharpen,100/format,webp/quality,Q_100", description = "Vincent Vega is a hitman and associate of Marsellus Wallace. He had a brother named Vic Vega who was shot and killed by an undercover cop while on a job. He worked in Amsterdam for over three years and recently returned to Los Angeles, where he has been partnered with Jules Winnfield.",viewType = RecyclingPagerAdapter.VIEW_TYPE_SUBSAMPLING_IMAGE),
        Poster(url = "$POSTERS_PATH/VCG41N1222442338.jpg", description = "The Driver - real name unknown - is a quiet man who has made a career out of stealing fast cars and using them as getaway vehicles in big-time robberies all over Los Angeles. Hot on the Driver's trail is the Detective (Bruce Dern), a conceited (and similarly nameless) cop who refers to the Driver as \"Cowboy\".",viewType = RecyclingPagerAdapter.VIEW_TYPE_IMAGE),
        Poster(url = "$POSTERS_PATH/VCG41N1267390623.jpg", description = "Frank Martin (Transporter) - he initially serves as a reluctant hero. He is portrayed as a former Special Forces operative who was a team leader of a search and destroy unit. His military background includes operations \"in and out of\" Lebanon, Syria and Sudan. He retires from this after becoming fatigued and disenchanted with his superior officers.",viewType = RecyclingPagerAdapter.VIEW_TYPE_IMAGE),
        Poster(url = "$POSTERS_PATH/VCG211294932890.jpg", description = "Maximillian \"Max\" Rockatansky started his apocalyptic adventure as a Main Force Patrol officer who fought for peace on the decaying roads of Australian civilization. Max served as the last line of defense against the reckless marauders terrorizing the roadways, driving a V8 Interceptor.",viewType = RecyclingPagerAdapter.VIEW_TYPE_IMAGE),
        Poster(url = "$POSTERS_PATH/VCG41N1219595814.jpg", description = "Daniel Morales - the fastest delivery man for the local pizza parlor Pizza Joe in Marseille, France. On the last day of work, he sets a new speed record, then leaves the job to pursue a new career as a taxi driver with the blessings of his boss and co-workers. Daniel's vehicle is a white 1997 Peugeot 406...",viewType = RecyclingPagerAdapter.VIEW_TYPE_IMAGE)
    )

    val horizontalImages = listOf(
        "$MISC_PATH/horizontal_colorful_1.jpg",
        "$MISC_PATH/square_colorful.jpg",
        "$MISC_PATH/horizontal_colorful_2.jpg",
        "$MISC_PATH/horizontal_colorful_3.jpg"
    )

    val verticalImages = listOf(
        "$MISC_PATH/vertical_colorful_1.jpg",
        "$MISC_PATH/vertical_colorful_2.jpg",
        "$MISC_PATH/vertical_colorful_3.jpg",
        "$MISC_PATH/vertical_colorful_4.jpg"
    )
}