package com.codexperiments.newsroot.data.local;

import com.codexperiments.newsroot.domain.entity.Tweet;
import com.codexperiments.newsroot.domain.entity.Tweet__JsonHelper;
import com.codexperiments.newsroot.domain.repository.TweetRepository;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TweetDatabaseRepository implements TweetRepository {
    private static final String JSON_FILE = "[\n" +
            "  {\n" +
            "    \"created_at\": \"Tue Jun 25 08:28:06 +0000 2013\",\n" +
            "    \"id\": 349443871694012400,\n" +
            "    \"id_str\": \"349443871694012418\",\n" +
            "    \"text\": \"Clément Méric : ce que disent les caméras de surveillance http://t.co/oFCsTY6ijH\",\n" +
            "    \"source\": \"<a href=\\\"http://dlvr.it\\\" rel=\\\"nofollow\\\">dlvr.it</a>\",\n" +
            "    \"truncated\": false,\n" +
            "    \"in_reply_to_status_id\": null,\n" +
            "    \"in_reply_to_status_id_str\": null,\n" +
            "    \"in_reply_to_user_id\": null,\n" +
            "    \"in_reply_to_user_id_str\": null,\n" +
            "    \"in_reply_to_screen_name\": null,\n" +
            "    \"user\": {\n" +
            "      \"id\": 24744541,\n" +
            "      \"id_str\": \"24744541\",\n" +
            "      \"name\": \"Le Monde\",\n" +
            "      \"screen_name\": \"lemondefr\",\n" +
            "      \"location\": \"Paris\",\n" +
            "      \"description\": \"Bienvenue sur le fil d'actualité du Monde.fr.\",\n" +
            "      \"url\": \"http://t.co/FAAzNQ8yF3\",\n" +
            "      \"entities\": {\n" +
            "        \"url\": {\n" +
            "          \"urls\": [\n" +
            "            {\n" +
            "              \"url\": \"http://t.co/FAAzNQ8yF3\",\n" +
            "              \"expanded_url\": \"http://www.lemonde.fr\",\n" +
            "              \"display_url\": \"lemonde.fr\",\n" +
            "              \"indices\": [\n" +
            "                0,\n" +
            "                22\n" +
            "              ]\n" +
            "            }\n" +
            "          ]\n" +
            "        },\n" +
            "        \"description\": {\n" +
            "          \"urls\": []\n" +
            "        }\n" +
            "      },\n" +
            "      \"protected\": false,\n" +
            "      \"followers_count\": 1596359,\n" +
            "      \"friends_count\": 226,\n" +
            "      \"listed_count\": 19776,\n" +
            "      \"created_at\": \"Mon Mar 16 18:44:51 +0000 2009\",\n" +
            "      \"favourites_count\": 107,\n" +
            "      \"utc_offset\": 3600,\n" +
            "      \"time_zone\": \"Paris\",\n" +
            "      \"geo_enabled\": false,\n" +
            "      \"verified\": true,\n" +
            "      \"statuses_count\": 82335,\n" +
            "      \"lang\": \"fr\",\n" +
            "      \"contributors_enabled\": false,\n" +
            "      \"is_translator\": false,\n" +
            "      \"profile_background_color\": \"DDE1EA\",\n" +
            "      \"profile_background_image_url\": \"http://a0.twimg.com/profile_background_images/255939997/twitter_lmfr.jpg\",\n" +
            "      \"profile_background_image_url_https\": \"https://si0.twimg.com/profile_background_images/255939997/twitter_lmfr.jpg\",\n" +
            "      \"profile_background_tile\": true,\n" +
            "      \"profile_image_url\": \"http://a0.twimg.com/profile_images/1430438688/IconeTwClassique_normal.jpg\",\n" +
            "      \"profile_image_url_https\": \"https://si0.twimg.com/profile_images/1430438688/IconeTwClassique_normal.jpg\",\n" +
            "      \"profile_link_color\": \"50B6CF\",\n" +
            "      \"profile_sidebar_border_color\": \"131316\",\n" +
            "      \"profile_sidebar_fill_color\": \"131316\",\n" +
            "      \"profile_text_color\": \"3292A8\",\n" +
            "      \"profile_use_background_image\": true,\n" +
            "      \"default_profile\": false,\n" +
            "      \"default_profile_image\": false,\n" +
            "      \"following\": true,\n" +
            "      \"follow_request_sent\": null,\n" +
            "      \"notifications\": null\n" +
            "    },\n" +
            "    \"geo\": null,\n" +
            "    \"coordinates\": null,\n" +
            "    \"place\": null,\n" +
            "    \"contributors\": null,\n" +
            "    \"retweet_count\": 102,\n" +
            "    \"favorite_count\": 13,\n" +
            "    \"entities\": {\n" +
            "      \"hashtags\": [],\n" +
            "      \"symbols\": [],\n" +
            "      \"urls\": [\n" +
            "        {\n" +
            "          \"url\": \"http://t.co/oFCsTY6ijH\",\n" +
            "          \"expanded_url\": \"http://lemde.fr/10Qard9\",\n" +
            "          \"display_url\": \"lemde.fr/10Qard9\",\n" +
            "          \"indices\": [\n" +
            "            58,\n" +
            "            80\n" +
            "          ]\n" +
            "        }\n" +
            "      ],\n" +
            "      \"user_mentions\": []\n" +
            "    },\n" +
            "    \"favorited\": false,\n" +
            "    \"retweeted\": false,\n" +
            "    \"possibly_sensitive\": false,\n" +
            "    \"lang\": \"fr\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"created_at\": \"Tue Jun 25 08:12:04 +0000 2013\",\n" +
            "    \"id\": 349439840015745000,\n" +
            "    \"id_str\": \"349439840015745024\",\n" +
            "    \"text\": \"ACDSee 16 disponible, toujours une référence ? http://t.co/rDVDzBeiFe\",\n" +
            "    \"source\": \"<a href=\\\"http://www.clubic.com\\\" rel=\\\"nofollow\\\">Clubic</a>\",\n" +
            "    \"truncated\": false,\n" +
            "    \"in_reply_to_status_id\": null,\n" +
            "    \"in_reply_to_status_id_str\": null,\n" +
            "    \"in_reply_to_user_id\": null,\n" +
            "    \"in_reply_to_user_id_str\": null,\n" +
            "    \"in_reply_to_screen_name\": null,\n" +
            "    \"user\": {\n" +
            "      \"id\": 18239824,\n" +
            "      \"id_str\": \"18239824\",\n" +
            "      \"name\": \"Clubic\",\n" +
            "      \"screen_name\": \"Clubic\",\n" +
            "      \"location\": \"\",\n" +
            "      \"description\": \"Toute l’actu #hitech : #news, #dossiers et #tests. \\r\\nBonus : #bonplans + #livetweet + #teasing.\",\n" +
            "      \"url\": \"http://t.co/4ipsYKyA8k\",\n" +
            "      \"entities\": {\n" +
            "        \"url\": {\n" +
            "          \"urls\": [\n" +
            "            {\n" +
            "              \"url\": \"http://t.co/4ipsYKyA8k\",\n" +
            "              \"expanded_url\": \"http://www.clubic.com\",\n" +
            "              \"display_url\": \"clubic.com\",\n" +
            "              \"indices\": [\n" +
            "                0,\n" +
            "                22\n" +
            "              ]\n" +
            "            }\n" +
            "          ]\n" +
            "        },\n" +
            "        \"description\": {\n" +
            "          \"urls\": []\n" +
            "        }\n" +
            "      },\n" +
            "      \"protected\": false,\n" +
            "      \"followers_count\": 31919,\n" +
            "      \"friends_count\": 193,\n" +
            "      \"listed_count\": 1316,\n" +
            "      \"created_at\": \"Fri Dec 19 11:16:35 +0000 2008\",\n" +
            "      \"favourites_count\": 5,\n" +
            "      \"utc_offset\": 3600,\n" +
            "      \"time_zone\": \"Paris\",\n" +
            "      \"geo_enabled\": true,\n" +
            "      \"verified\": false,\n" +
            "      \"statuses_count\": 35251,\n" +
            "      \"lang\": \"fr\",\n" +
            "      \"contributors_enabled\": false,\n" +
            "      \"is_translator\": false,\n" +
            "      \"profile_background_color\": \"131516\",\n" +
            "      \"profile_background_image_url\": \"http://a0.twimg.com/profile_background_images/92688003/bg_home.jpg\",\n" +
            "      \"profile_background_image_url_https\": \"https://si0.twimg.com/profile_background_images/92688003/bg_home.jpg\",\n" +
            "      \"profile_background_tile\": true,\n" +
            "      \"profile_image_url\": \"http://a0.twimg.com/profile_images/1148040974/200-200_normal.png\",\n" +
            "      \"profile_image_url_https\": \"https://si0.twimg.com/profile_images/1148040974/200-200_normal.png\",\n" +
            "      \"profile_banner_url\": \"https://pbs.twimg.com/profile_banners/18239824/1347980725\",\n" +
            "      \"profile_link_color\": \"E1190D\",\n" +
            "      \"profile_sidebar_border_color\": \"000000\",\n" +
            "      \"profile_sidebar_fill_color\": \"FFFFFF\",\n" +
            "      \"profile_text_color\": \"000000\",\n" +
            "      \"profile_use_background_image\": true,\n" +
            "      \"default_profile\": false,\n" +
            "      \"default_profile_image\": false,\n" +
            "      \"following\": true,\n" +
            "      \"follow_request_sent\": null,\n" +
            "      \"notifications\": null\n" +
            "    },\n" +
            "    \"geo\": null,\n" +
            "    \"coordinates\": null,\n" +
            "    \"place\": null,\n" +
            "    \"contributors\": null,\n" +
            "    \"retweet_count\": 1,\n" +
            "    \"favorite_count\": 0,\n" +
            "    \"entities\": {\n" +
            "      \"hashtags\": [],\n" +
            "      \"symbols\": [],\n" +
            "      \"urls\": [\n" +
            "        {\n" +
            "          \"url\": \"http://t.co/rDVDzBeiFe\",\n" +
            "          \"expanded_url\": \"http://bit.ly/1a9QbpW\",\n" +
            "          \"display_url\": \"bit.ly/1a9QbpW\",\n" +
            "          \"indices\": [\n" +
            "            47,\n" +
            "            69\n" +
            "          ]\n" +
            "        }\n" +
            "      ],\n" +
            "      \"user_mentions\": []\n" +
            "    },\n" +
            "    \"favorited\": false,\n" +
            "    \"retweeted\": false,\n" +
            "    \"possibly_sensitive\": false,\n" +
            "    \"lang\": \"fr\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"created_at\": \"Tue Jun 25 08:00:48 +0000 2013\",\n" +
            "    \"id\": 349437004410400800,\n" +
            "    \"id_str\": \"349437004410400769\",\n" +
            "    \"text\": \"RT @LG_Blog_France: Test du #LG Optimus G Pro (E986) à lire chez @twandroid http://t.co/gQOA38kTWd\",\n" +
            "    \"source\": \"<a href=\\\"http://itunes.apple.com/us/app/twitter/id409789998?mt=12\\\" rel=\\\"nofollow\\\">Twitter for Mac</a>\",\n" +
            "    \"truncated\": false,\n" +
            "    \"in_reply_to_status_id\": null,\n" +
            "    \"in_reply_to_status_id_str\": null,\n" +
            "    \"in_reply_to_user_id\": null,\n" +
            "    \"in_reply_to_user_id_str\": null,\n" +
            "    \"in_reply_to_screen_name\": null,\n" +
            "    \"user\": {\n" +
            "      \"id\": 25260412,\n" +
            "      \"id_str\": \"25260412\",\n" +
            "      \"name\": \"FrAndroid\",\n" +
            "      \"screen_name\": \"twandroid\",\n" +
            "      \"location\": \"Android World\",\n" +
            "      \"description\": \"Première communauté Android francophone et seconde mondiale  Toute l'actu #Android : #applis, #jeux, #smartphones, #tests\",\n" +
            "      \"url\": \"http://t.co/M4dhAG94kU\",\n" +
            "      \"entities\": {\n" +
            "        \"url\": {\n" +
            "          \"urls\": [\n" +
            "            {\n" +
            "              \"url\": \"http://t.co/M4dhAG94kU\",\n" +
            "              \"expanded_url\": \"http://www.frandroid.com/\",\n" +
            "              \"display_url\": \"frandroid.com\",\n" +
            "              \"indices\": [\n" +
            "                0,\n" +
            "                22\n" +
            "              ]\n" +
            "            }\n" +
            "          ]\n" +
            "        },\n" +
            "        \"description\": {\n" +
            "          \"urls\": []\n" +
            "        }\n" +
            "      },\n" +
            "      \"protected\": false,\n" +
            "      \"followers_count\": 29075,\n" +
            "      \"friends_count\": 3776,\n" +
            "      \"listed_count\": 1295,\n" +
            "      \"created_at\": \"Thu Mar 19 08:45:51 +0000 2009\",\n" +
            "      \"favourites_count\": 24,\n" +
            "      \"utc_offset\": null,\n" +
            "      \"time_zone\": null,\n" +
            "      \"geo_enabled\": true,\n" +
            "      \"verified\": false,\n" +
            "      \"statuses_count\": 21414,\n" +
            "      \"lang\": \"fr\",\n" +
            "      \"contributors_enabled\": false,\n" +
            "      \"is_translator\": false,\n" +
            "      \"profile_background_color\": \"131516\",\n" +
            "      \"profile_background_image_url\": \"http://a0.twimg.com/images/themes/theme14/bg.gif\",\n" +
            "      \"profile_background_image_url_https\": \"https://si0.twimg.com/images/themes/theme14/bg.gif\",\n" +
            "      \"profile_background_tile\": true,\n" +
            "      \"profile_image_url\": \"http://a0.twimg.com/profile_images/1865300480/FACEBOOK-AVATAR_normal.jpg\",\n" +
            "      \"profile_image_url_https\": \"https://si0.twimg.com/profile_images/1865300480/FACEBOOK-AVATAR_normal.jpg\",\n" +
            "      \"profile_link_color\": \"009999\",\n" +
            "      \"profile_sidebar_border_color\": \"EEEEEE\",\n" +
            "      \"profile_sidebar_fill_color\": \"EFEFEF\",\n" +
            "      \"profile_text_color\": \"333333\",\n" +
            "      \"profile_use_background_image\": true,\n" +
            "      \"default_profile\": false,\n" +
            "      \"default_profile_image\": false,\n" +
            "      \"following\": true,\n" +
            "      \"follow_request_sent\": null,\n" +
            "      \"notifications\": null\n" +
            "    },\n" +
            "    \"geo\": null,\n" +
            "    \"coordinates\": null,\n" +
            "    \"place\": null,\n" +
            "    \"contributors\": null,\n" +
            "    \"retweeted_status\": {\n" +
            "      \"created_at\": \"Tue Jun 25 07:48:31 +0000 2013\",\n" +
            "      \"id\": 349433913845874700,\n" +
            "      \"id_str\": \"349433913845874689\",\n" +
            "      \"text\": \"Test du #LG Optimus G Pro (E986) à lire chez @twandroid http://t.co/gQOA38kTWd\",\n" +
            "      \"source\": \"<a href=\\\"http://twitter.com/tweetbutton\\\" rel=\\\"nofollow\\\">Tweet Button</a>\",\n" +
            "      \"truncated\": false,\n" +
            "      \"in_reply_to_status_id\": null,\n" +
            "      \"in_reply_to_status_id_str\": null,\n" +
            "      \"in_reply_to_user_id\": null,\n" +
            "      \"in_reply_to_user_id_str\": null,\n" +
            "      \"in_reply_to_screen_name\": null,\n" +
            "      \"user\": {\n" +
            "        \"id\": 18982329,\n" +
            "        \"id_str\": \"18982329\",\n" +
            "        \"name\": \"LG_France\",\n" +
            "        \"screen_name\": \"LG_Blog_France\",\n" +
            "        \"location\": \"\",\n" +
            "        \"description\": \"Compte officiel de LG Electronics France\\r\\n#LG #SmartTV #WeareLG\\r\\nhttps://t.co/ET3EKcPcjy\",\n" +
            "        \"url\": \"http://t.co/sZSZIuo9WG\",\n" +
            "        \"entities\": {\n" +
            "          \"url\": {\n" +
            "            \"urls\": [\n" +
            "              {\n" +
            "                \"url\": \"http://t.co/sZSZIuo9WG\",\n" +
            "                \"expanded_url\": \"http://www.lgblog.fr/\",\n" +
            "                \"display_url\": \"lgblog.fr\",\n" +
            "                \"indices\": [\n" +
            "                  0,\n" +
            "                  22\n" +
            "                ]\n" +
            "              }\n" +
            "            ]\n" +
            "          },\n" +
            "          \"description\": {\n" +
            "            \"urls\": [\n" +
            "              {\n" +
            "                \"url\": \"https://t.co/ET3EKcPcjy\",\n" +
            "                \"expanded_url\": \"https://www.facebook.com/lgfrance\",\n" +
            "                \"display_url\": \"facebook.com/lgfrance\",\n" +
            "                \"indices\": [\n" +
            "                  65,\n" +
            "                  88\n" +
            "                ]\n" +
            "              }\n" +
            "            ]\n" +
            "          }\n" +
            "        },\n" +
            "        \"protected\": false,\n" +
            "        \"followers_count\": 5812,\n" +
            "        \"friends_count\": 994,\n" +
            "        \"listed_count\": 215,\n" +
            "        \"created_at\": \"Wed Jan 14 16:43:22 +0000 2009\",\n" +
            "        \"favourites_count\": 19,\n" +
            "        \"utc_offset\": 3600,\n" +
            "        \"time_zone\": \"Paris\",\n" +
            "        \"geo_enabled\": true,\n" +
            "        \"verified\": false,\n" +
            "        \"statuses_count\": 2060,\n" +
            "        \"lang\": \"fr\",\n" +
            "        \"contributors_enabled\": false,\n" +
            "        \"is_translator\": false,\n" +
            "        \"profile_background_color\": \"FFFFFF\",\n" +
            "        \"profile_background_image_url\": \"http://a0.twimg.com/profile_background_images/344511763024825455/ac878934176ecc68c1d080cc28dced1a.png\",\n" +
            "        \"profile_background_image_url_https\": \"https://si0.twimg.com/profile_background_images/344511763024825455/ac878934176ecc68c1d080cc28dced1a.png\",\n" +
            "        \"profile_background_tile\": false,\n" +
            "        \"profile_image_url\": \"http://a0.twimg.com/profile_images/2789521590/483e1291fdab25d2509728c9bedf556a_normal.png\",\n" +
            "        \"profile_image_url_https\": \"https://si0.twimg.com/profile_images/2789521590/483e1291fdab25d2509728c9bedf556a_normal.png\",\n" +
            "        \"profile_banner_url\": \"https://pbs.twimg.com/profile_banners/18982329/1371024423\",\n" +
            "        \"profile_link_color\": \"1F98C7\",\n" +
            "        \"profile_sidebar_border_color\": \"FFFFFF\",\n" +
            "        \"profile_sidebar_fill_color\": \"EFEFEF\",\n" +
            "        \"profile_text_color\": \"333333\",\n" +
            "        \"profile_use_background_image\": true,\n" +
            "        \"default_profile\": false,\n" +
            "        \"default_profile_image\": false,\n" +
            "        \"following\": null,\n" +
            "        \"follow_request_sent\": null,\n" +
            "        \"notifications\": null\n" +
            "      },\n" +
            "      \"geo\": null,\n" +
            "      \"coordinates\": null,\n" +
            "      \"place\": null,\n" +
            "      \"contributors\": null,\n" +
            "      \"retweet_count\": 2,\n" +
            "      \"favorite_count\": 0,\n" +
            "      \"entities\": {\n" +
            "        \"hashtags\": [\n" +
            "          {\n" +
            "            \"text\": \"LG\",\n" +
            "            \"indices\": [\n" +
            "              8,\n" +
            "              11\n" +
            "            ]\n" +
            "          }\n" +
            "        ],\n" +
            "        \"symbols\": [],\n" +
            "        \"urls\": [\n" +
            "          {\n" +
            "            \"url\": \"http://t.co/gQOA38kTWd\",\n" +
            "            \"expanded_url\": \"http://www.frandroid.com/test/147924_test-du-lg-optimus-g-pro-e986\",\n" +
            "            \"display_url\": \"frandroid.com/test/147924_te…\",\n" +
            "            \"indices\": [\n" +
            "              56,\n" +
            "              78\n" +
            "            ]\n" +
            "          }\n" +
            "        ],\n" +
            "        \"user_mentions\": [\n" +
            "          {\n" +
            "            \"screen_name\": \"twandroid\",\n" +
            "            \"name\": \"FrAndroid\",\n" +
            "            \"id\": 25260412,\n" +
            "            \"id_str\": \"25260412\",\n" +
            "            \"indices\": [\n" +
            "              45,\n" +
            "              55\n" +
            "            ]\n" +
            "          }\n" +
            "        ]\n" +
            "      },\n" +
            "      \"favorited\": false,\n" +
            "      \"retweeted\": false,\n" +
            "      \"possibly_sensitive\": false,\n" +
            "      \"lang\": \"fr\"\n" +
            "    },\n" +
            "    \"retweet_count\": 2,\n" +
            "    \"favorite_count\": 0,\n" +
            "    \"entities\": {\n" +
            "      \"hashtags\": [\n" +
            "        {\n" +
            "          \"text\": \"LG\",\n" +
            "          \"indices\": [\n" +
            "            28,\n" +
            "            31\n" +
            "          ]\n" +
            "        }\n" +
            "      ],\n" +
            "      \"symbols\": [],\n" +
            "      \"urls\": [\n" +
            "        {\n" +
            "          \"url\": \"http://t.co/gQOA38kTWd\",\n" +
            "          \"expanded_url\": \"http://www.frandroid.com/test/147924_test-du-lg-optimus-g-pro-e986\",\n" +
            "          \"display_url\": \"frandroid.com/test/147924_te…\",\n" +
            "          \"indices\": [\n" +
            "            76,\n" +
            "            98\n" +
            "          ]\n" +
            "        }\n" +
            "      ],\n" +
            "      \"user_mentions\": [\n" +
            "        {\n" +
            "          \"screen_name\": \"LG_Blog_France\",\n" +
            "          \"name\": \"LG_France\",\n" +
            "          \"id\": 18982329,\n" +
            "          \"id_str\": \"18982329\",\n" +
            "          \"indices\": [\n" +
            "            3,\n" +
            "            18\n" +
            "          ]\n" +
            "        },\n" +
            "        {\n" +
            "          \"screen_name\": \"twandroid\",\n" +
            "          \"name\": \"FrAndroid\",\n" +
            "          \"id\": 25260412,\n" +
            "          \"id_str\": \"25260412\",\n" +
            "          \"indices\": [\n" +
            "            65,\n" +
            "            75\n" +
            "          ]\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    \"favorited\": false,\n" +
            "    \"retweeted\": false,\n" +
            "    \"possibly_sensitive\": false,\n" +
            "    \"lang\": \"fr\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"created_at\": \"Tue Jun 25 07:30:58 +0000 2013\",\n" +
            "    \"id\": 349429493900906500,\n" +
            "    \"id_str\": \"349429493900906498\",\n" +
            "    \"text\": \"Immobilier: le neuf ralentit sa chute http://t.co/XwS77O4YJ6\",\n" +
            "    \"source\": \"<a href=\\\"http://twitter.com/tweetbutton\\\" rel=\\\"nofollow\\\">Tweet Button</a>\",\n" +
            "    \"truncated\": false,\n" +
            "    \"in_reply_to_status_id\": null,\n" +
            "    \"in_reply_to_status_id_str\": null,\n" +
            "    \"in_reply_to_user_id\": null,\n" +
            "    \"in_reply_to_user_id_str\": null,\n" +
            "    \"in_reply_to_screen_name\": null,\n" +
            "    \"user\": {\n" +
            "      \"id\": 140776364,\n" +
            "      \"id_str\": \"140776364\",\n" +
            "      \"name\": \"BFM Business\",\n" +
            "      \"screen_name\": \"bfmbusiness\",\n" +
            "      \"location\": \"\",\n" +
            "      \"description\": \"Compte officiel de BFM Business\",\n" +
            "      \"url\": \"http://t.co/JBiTL29ifZ\",\n" +
            "      \"entities\": {\n" +
            "        \"url\": {\n" +
            "          \"urls\": [\n" +
            "            {\n" +
            "              \"url\": \"http://t.co/JBiTL29ifZ\",\n" +
            "              \"expanded_url\": \"http://www.bfmbusiness.com\",\n" +
            "              \"display_url\": \"bfmbusiness.com\",\n" +
            "              \"indices\": [\n" +
            "                0,\n" +
            "                22\n" +
            "              ]\n" +
            "            }\n" +
            "          ]\n" +
            "        },\n" +
            "        \"description\": {\n" +
            "          \"urls\": []\n" +
            "        }\n" +
            "      },\n" +
            "      \"protected\": false,\n" +
            "      \"followers_count\": 27619,\n" +
            "      \"friends_count\": 353,\n" +
            "      \"listed_count\": 662,\n" +
            "      \"created_at\": \"Thu May 06 10:21:56 +0000 2010\",\n" +
            "      \"favourites_count\": 0,\n" +
            "      \"utc_offset\": 3600,\n" +
            "      \"time_zone\": \"Paris\",\n" +
            "      \"geo_enabled\": false,\n" +
            "      \"verified\": false,\n" +
            "      \"statuses_count\": 6606,\n" +
            "      \"lang\": \"fr\",\n" +
            "      \"contributors_enabled\": false,\n" +
            "      \"is_translator\": false,\n" +
            "      \"profile_background_color\": \"C0DEED\",\n" +
            "      \"profile_background_image_url\": \"http://a0.twimg.com/images/themes/theme1/bg.png\",\n" +
            "      \"profile_background_image_url_https\": \"https://si0.twimg.com/images/themes/theme1/bg.png\",\n" +
            "      \"profile_background_tile\": false,\n" +
            "      \"profile_image_url\": \"http://a0.twimg.com/profile_images/1277958396/icon-403747057-175x175_normal.jpg\",\n" +
            "      \"profile_image_url_https\": \"https://si0.twimg.com/profile_images/1277958396/icon-403747057-175x175_normal.jpg\",\n" +
            "      \"profile_link_color\": \"0084B4\",\n" +
            "      \"profile_sidebar_border_color\": \"C0DEED\",\n" +
            "      \"profile_sidebar_fill_color\": \"DDEEF6\",\n" +
            "      \"profile_text_color\": \"333333\",\n" +
            "      \"profile_use_background_image\": true,\n" +
            "      \"default_profile\": true,\n" +
            "      \"default_profile_image\": false,\n" +
            "      \"following\": true,\n" +
            "      \"follow_request_sent\": null,\n" +
            "      \"notifications\": null\n" +
            "    },\n" +
            "    \"geo\": null,\n" +
            "    \"coordinates\": null,\n" +
            "    \"place\": null,\n" +
            "    \"contributors\": null,\n" +
            "    \"retweet_count\": 9,\n" +
            "    \"favorite_count\": 4,\n" +
            "    \"entities\": {\n" +
            "      \"hashtags\": [],\n" +
            "      \"symbols\": [],\n" +
            "      \"urls\": [\n" +
            "        {\n" +
            "          \"url\": \"http://t.co/XwS77O4YJ6\",\n" +
            "          \"expanded_url\": \"http://www.bfmtv.com/economie/immobilier-neuf-ralentit-chute-545340.html\",\n" +
            "          \"display_url\": \"bfmtv.com/economie/immob…\",\n" +
            "          \"indices\": [\n" +
            "            38,\n" +
            "            60\n" +
            "          ]\n" +
            "        }\n" +
            "      ],\n" +
            "      \"user_mentions\": []\n" +
            "    },\n" +
            "    \"favorited\": false,\n" +
            "    \"retweeted\": false,\n" +
            "    \"possibly_sensitive\": false,\n" +
            "    \"lang\": \"fr\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"created_at\": \"Tue Jun 25 07:30:52 +0000 2013\",\n" +
            "    \"id\": 349429468894478340,\n" +
            "    \"id_str\": \"349429468894478336\",\n" +
            "    \"text\": \"Laurent #Jalabert : \\\"Je ne peux pas dire que ce soit faux, je ne peux pas dire que ce soit vrai\\\" http://t.co/8vz7sE0VhL\",\n" +
            "    \"source\": \"<a href=\\\"http://www.tweetdeck.com\\\" rel=\\\"nofollow\\\">TweetDeck</a>\",\n" +
            "    \"truncated\": false,\n" +
            "    \"in_reply_to_status_id\": null,\n" +
            "    \"in_reply_to_status_id_str\": null,\n" +
            "    \"in_reply_to_user_id\": null,\n" +
            "    \"in_reply_to_user_id_str\": null,\n" +
            "    \"in_reply_to_screen_name\": null,\n" +
            "    \"user\": {\n" +
            "      \"id\": 24744541,\n" +
            "      \"id_str\": \"24744541\",\n" +
            "      \"name\": \"Le Monde\",\n" +
            "      \"screen_name\": \"lemondefr\",\n" +
            "      \"location\": \"Paris\",\n" +
            "      \"description\": \"Bienvenue sur le fil d'actualité du Monde.fr.\",\n" +
            "      \"url\": \"http://t.co/FAAzNQ8yF3\",\n" +
            "      \"entities\": {\n" +
            "        \"url\": {\n" +
            "          \"urls\": [\n" +
            "            {\n" +
            "              \"url\": \"http://t.co/FAAzNQ8yF3\",\n" +
            "              \"expanded_url\": \"http://www.lemonde.fr\",\n" +
            "              \"display_url\": \"lemonde.fr\",\n" +
            "              \"indices\": [\n" +
            "                0,\n" +
            "                22\n" +
            "              ]\n" +
            "            }\n" +
            "          ]\n" +
            "        },\n" +
            "        \"description\": {\n" +
            "          \"urls\": []\n" +
            "        }\n" +
            "      },\n" +
            "      \"protected\": false,\n" +
            "      \"followers_count\": 1596359,\n" +
            "      \"friends_count\": 226,\n" +
            "      \"listed_count\": 19776,\n" +
            "      \"created_at\": \"Mon Mar 16 18:44:51 +0000 2009\",\n" +
            "      \"favourites_count\": 107,\n" +
            "      \"utc_offset\": 3600,\n" +
            "      \"time_zone\": \"Paris\",\n" +
            "      \"geo_enabled\": false,\n" +
            "      \"verified\": true,\n" +
            "      \"statuses_count\": 82335,\n" +
            "      \"lang\": \"fr\",\n" +
            "      \"contributors_enabled\": false,\n" +
            "      \"is_translator\": false,\n" +
            "      \"profile_background_color\": \"DDE1EA\",\n" +
            "      \"profile_background_image_url\": \"http://a0.twimg.com/profile_background_images/255939997/twitter_lmfr.jpg\",\n" +
            "      \"profile_background_image_url_https\": \"https://si0.twimg.com/profile_background_images/255939997/twitter_lmfr.jpg\",\n" +
            "      \"profile_background_tile\": true,\n" +
            "      \"profile_image_url\": \"http://a0.twimg.com/profile_images/1430438688/IconeTwClassique_normal.jpg\",\n" +
            "      \"profile_image_url_https\": \"https://si0.twimg.com/profile_images/1430438688/IconeTwClassique_normal.jpg\",\n" +
            "      \"profile_link_color\": \"50B6CF\",\n" +
            "      \"profile_sidebar_border_color\": \"131316\",\n" +
            "      \"profile_sidebar_fill_color\": \"131316\",\n" +
            "      \"profile_text_color\": \"3292A8\",\n" +
            "      \"profile_use_background_image\": true,\n" +
            "      \"default_profile\": false,\n" +
            "      \"default_profile_image\": false,\n" +
            "      \"following\": true,\n" +
            "      \"follow_request_sent\": null,\n" +
            "      \"notifications\": null\n" +
            "    },\n" +
            "    \"geo\": null,\n" +
            "    \"coordinates\": null,\n" +
            "    \"place\": null,\n" +
            "    \"contributors\": null,\n" +
            "    \"retweet_count\": 37,\n" +
            "    \"favorite_count\": 3,\n" +
            "    \"entities\": {\n" +
            "      \"hashtags\": [\n" +
            "        {\n" +
            "          \"text\": \"Jalabert\",\n" +
            "          \"indices\": [\n" +
            "            8,\n" +
            "            17\n" +
            "          ]\n" +
            "        }\n" +
            "      ],\n" +
            "      \"symbols\": [],\n" +
            "      \"urls\": [\n" +
            "        {\n" +
            "          \"url\": \"http://t.co/8vz7sE0VhL\",\n" +
            "          \"expanded_url\": \"http://lemde.fr/14Uzzyx\",\n" +
            "          \"display_url\": \"lemde.fr/14Uzzyx\",\n" +
            "          \"indices\": [\n" +
            "            97,\n" +
            "            119\n" +
            "          ]\n" +
            "        }\n" +
            "      ],\n" +
            "      \"user_mentions\": []\n" +
            "    },\n" +
            "    \"favorited\": false,\n" +
            "    \"retweeted\": false,\n" +
            "    \"possibly_sensitive\": false,\n" +
            "    \"lang\": \"fr\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"created_at\": \"Tue Jun 25 07:28:34 +0000 2013\",\n" +
            "    \"id\": 349428889900163100,\n" +
            "    \"id_str\": \"349428889900163072\",\n" +
            "    \"text\": \"L'opposition tire le signal d'alarme sur les déficits http://t.co/53fJ7IMcXy\",\n" +
            "    \"source\": \"<a href=\\\"http://dlvr.it\\\" rel=\\\"nofollow\\\">dlvr.it</a>\",\n" +
            "    \"truncated\": false,\n" +
            "    \"in_reply_to_status_id\": null,\n" +
            "    \"in_reply_to_status_id_str\": null,\n" +
            "    \"in_reply_to_user_id\": null,\n" +
            "    \"in_reply_to_user_id_str\": null,\n" +
            "    \"in_reply_to_screen_name\": null,\n" +
            "    \"user\": {\n" +
            "      \"id\": 24744541,\n" +
            "      \"id_str\": \"24744541\",\n" +
            "      \"name\": \"Le Monde\",\n" +
            "      \"screen_name\": \"lemondefr\",\n" +
            "      \"location\": \"Paris\",\n" +
            "      \"description\": \"Bienvenue sur le fil d'actualité du Monde.fr.\",\n" +
            "      \"url\": \"http://t.co/FAAzNQ8yF3\",\n" +
            "      \"entities\": {\n" +
            "        \"url\": {\n" +
            "          \"urls\": [\n" +
            "            {\n" +
            "              \"url\": \"http://t.co/FAAzNQ8yF3\",\n" +
            "              \"expanded_url\": \"http://www.lemonde.fr\",\n" +
            "              \"display_url\": \"lemonde.fr\",\n" +
            "              \"indices\": [\n" +
            "                0,\n" +
            "                22\n" +
            "              ]\n" +
            "            }\n" +
            "          ]\n" +
            "        },\n" +
            "        \"description\": {\n" +
            "          \"urls\": []\n" +
            "        }\n" +
            "      },\n" +
            "      \"protected\": false,\n" +
            "      \"followers_count\": 1596359,\n" +
            "      \"friends_count\": 226,\n" +
            "      \"listed_count\": 19776,\n" +
            "      \"created_at\": \"Mon Mar 16 18:44:51 +0000 2009\",\n" +
            "      \"favourites_count\": 107,\n" +
            "      \"utc_offset\": 3600,\n" +
            "      \"time_zone\": \"Paris\",\n" +
            "      \"geo_enabled\": false,\n" +
            "      \"verified\": true,\n" +
            "      \"statuses_count\": 82335,\n" +
            "      \"lang\": \"fr\",\n" +
            "      \"contributors_enabled\": false,\n" +
            "      \"is_translator\": false,\n" +
            "      \"profile_background_color\": \"DDE1EA\",\n" +
            "      \"profile_background_image_url\": \"http://a0.twimg.com/profile_background_images/255939997/twitter_lmfr.jpg\",\n" +
            "      \"profile_background_image_url_https\": \"https://si0.twimg.com/profile_background_images/255939997/twitter_lmfr.jpg\",\n" +
            "      \"profile_background_tile\": true,\n" +
            "      \"profile_image_url\": \"http://a0.twimg.com/profile_images/1430438688/IconeTwClassique_normal.jpg\",\n" +
            "      \"profile_image_url_https\": \"https://si0.twimg.com/profile_images/1430438688/IconeTwClassique_normal.jpg\",\n" +
            "      \"profile_link_color\": \"50B6CF\",\n" +
            "      \"profile_sidebar_border_color\": \"131316\",\n" +
            "      \"profile_sidebar_fill_color\": \"131316\",\n" +
            "      \"profile_text_color\": \"3292A8\",\n" +
            "      \"profile_use_background_image\": true,\n" +
            "      \"default_profile\": false,\n" +
            "      \"default_profile_image\": false,\n" +
            "      \"following\": true,\n" +
            "      \"follow_request_sent\": null,\n" +
            "      \"notifications\": null\n" +
            "    },\n" +
            "    \"geo\": null,\n" +
            "    \"coordinates\": null,\n" +
            "    \"place\": null,\n" +
            "    \"contributors\": null,\n" +
            "    \"retweet_count\": 7,\n" +
            "    \"favorite_count\": 1,\n" +
            "    \"entities\": {\n" +
            "      \"hashtags\": [],\n" +
            "      \"symbols\": [],\n" +
            "      \"urls\": [\n" +
            "        {\n" +
            "          \"url\": \"http://t.co/53fJ7IMcXy\",\n" +
            "          \"expanded_url\": \"http://lemde.fr/1ch2k8Y\",\n" +
            "          \"display_url\": \"lemde.fr/1ch2k8Y\",\n" +
            "          \"indices\": [\n" +
            "            54,\n" +
            "            76\n" +
            "          ]\n" +
            "        }\n" +
            "      ],\n" +
            "      \"user_mentions\": []\n" +
            "    },\n" +
            "    \"favorited\": false,\n" +
            "    \"retweeted\": false,\n" +
            "    \"possibly_sensitive\": false,\n" +
            "    \"lang\": \"fr\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"created_at\": \"Tue Jun 25 07:26:05 +0000 2013\",\n" +
            "    \"id\": 349428264630100000,\n" +
            "    \"id_str\": \"349428264630099969\",\n" +
            "    \"text\": \"Reconnaissance de mouvements : dernière ligne droite pour Leap Motion http://t.co/eydvc86yet\",\n" +
            "    \"source\": \"<a href=\\\"http://www.clubic.com\\\" rel=\\\"nofollow\\\">Clubic</a>\",\n" +
            "    \"truncated\": false,\n" +
            "    \"in_reply_to_status_id\": null,\n" +
            "    \"in_reply_to_status_id_str\": null,\n" +
            "    \"in_reply_to_user_id\": null,\n" +
            "    \"in_reply_to_user_id_str\": null,\n" +
            "    \"in_reply_to_screen_name\": null,\n" +
            "    \"user\": {\n" +
            "      \"id\": 18239824,\n" +
            "      \"id_str\": \"18239824\",\n" +
            "      \"name\": \"Clubic\",\n" +
            "      \"screen_name\": \"Clubic\",\n" +
            "      \"location\": \"\",\n" +
            "      \"description\": \"Toute l’actu #hitech : #news, #dossiers et #tests. \\r\\nBonus : #bonplans + #livetweet + #teasing.\",\n" +
            "      \"url\": \"http://t.co/4ipsYKyA8k\",\n" +
            "      \"entities\": {\n" +
            "        \"url\": {\n" +
            "          \"urls\": [\n" +
            "            {\n" +
            "              \"url\": \"http://t.co/4ipsYKyA8k\",\n" +
            "              \"expanded_url\": \"http://www.clubic.com\",\n" +
            "              \"display_url\": \"clubic.com\",\n" +
            "              \"indices\": [\n" +
            "                0,\n" +
            "                22\n" +
            "              ]\n" +
            "            }\n" +
            "          ]\n" +
            "        },\n" +
            "        \"description\": {\n" +
            "          \"urls\": []\n" +
            "        }\n" +
            "      },\n" +
            "      \"protected\": false,\n" +
            "      \"followers_count\": 31919,\n" +
            "      \"friends_count\": 193,\n" +
            "      \"listed_count\": 1316,\n" +
            "      \"created_at\": \"Fri Dec 19 11:16:35 +0000 2008\",\n" +
            "      \"favourites_count\": 5,\n" +
            "      \"utc_offset\": 3600,\n" +
            "      \"time_zone\": \"Paris\",\n" +
            "      \"geo_enabled\": true,\n" +
            "      \"verified\": false,\n" +
            "      \"statuses_count\": 35251,\n" +
            "      \"lang\": \"fr\",\n" +
            "      \"contributors_enabled\": false,\n" +
            "      \"is_translator\": false,\n" +
            "      \"profile_background_color\": \"131516\",\n" +
            "      \"profile_background_image_url\": \"http://a0.twimg.com/profile_background_images/92688003/bg_home.jpg\",\n" +
            "      \"profile_background_image_url_https\": \"https://si0.twimg.com/profile_background_images/92688003/bg_home.jpg\",\n" +
            "      \"profile_background_tile\": true,\n" +
            "      \"profile_image_url\": \"http://a0.twimg.com/profile_images/1148040974/200-200_normal.png\",\n" +
            "      \"profile_image_url_https\": \"https://si0.twimg.com/profile_images/1148040974/200-200_normal.png\",\n" +
            "      \"profile_banner_url\": \"https://pbs.twimg.com/profile_banners/18239824/1347980725\",\n" +
            "      \"profile_link_color\": \"E1190D\",\n" +
            "      \"profile_sidebar_border_color\": \"000000\",\n" +
            "      \"profile_sidebar_fill_color\": \"FFFFFF\",\n" +
            "      \"profile_text_color\": \"000000\",\n" +
            "      \"profile_use_background_image\": true,\n" +
            "      \"default_profile\": false,\n" +
            "      \"default_profile_image\": false,\n" +
            "      \"following\": true,\n" +
            "      \"follow_request_sent\": null,\n" +
            "      \"notifications\": null\n" +
            "    },\n" +
            "    \"geo\": null,\n" +
            "    \"coordinates\": null,\n" +
            "    \"place\": null,\n" +
            "    \"contributors\": null,\n" +
            "    \"retweet_count\": 2,\n" +
            "    \"favorite_count\": 1,\n" +
            "    \"entities\": {\n" +
            "      \"hashtags\": [],\n" +
            "      \"symbols\": [],\n" +
            "      \"urls\": [\n" +
            "        {\n" +
            "          \"url\": \"http://t.co/eydvc86yet\",\n" +
            "          \"expanded_url\": \"http://bit.ly/1a9K2tI\",\n" +
            "          \"display_url\": \"bit.ly/1a9K2tI\",\n" +
            "          \"indices\": [\n" +
            "            70,\n" +
            "            92\n" +
            "          ]\n" +
            "        }\n" +
            "      ],\n" +
            "      \"user_mentions\": []\n" +
            "    },\n" +
            "    \"favorited\": false,\n" +
            "    \"retweeted\": false,\n" +
            "    \"possibly_sensitive\": false,\n" +
            "    \"lang\": \"fr\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"created_at\": \"Tue Jun 25 07:24:51 +0000 2013\",\n" +
            "    \"id\": 349427954293555200,\n" +
            "    \"id_str\": \"349427954293555200\",\n" +
            "    \"text\": \"Tamim Al-Thani, un héritier prudent à la tête du #Qatar http://t.co/Vfp6RrjsSJ\",\n" +
            "    \"source\": \"<a href=\\\"http://www.tweetdeck.com\\\" rel=\\\"nofollow\\\">TweetDeck</a>\",\n" +
            "    \"truncated\": false,\n" +
            "    \"in_reply_to_status_id\": null,\n" +
            "    \"in_reply_to_status_id_str\": null,\n" +
            "    \"in_reply_to_user_id\": null,\n" +
            "    \"in_reply_to_user_id_str\": null,\n" +
            "    \"in_reply_to_screen_name\": null,\n" +
            "    \"user\": {\n" +
            "      \"id\": 24744541,\n" +
            "      \"id_str\": \"24744541\",\n" +
            "      \"name\": \"Le Monde\",\n" +
            "      \"screen_name\": \"lemondefr\",\n" +
            "      \"location\": \"Paris\",\n" +
            "      \"description\": \"Bienvenue sur le fil d'actualité du Monde.fr.\",\n" +
            "      \"url\": \"http://t.co/FAAzNQ8yF3\",\n" +
            "      \"entities\": {\n" +
            "        \"url\": {\n" +
            "          \"urls\": [\n" +
            "            {\n" +
            "              \"url\": \"http://t.co/FAAzNQ8yF3\",\n" +
            "              \"expanded_url\": \"http://www.lemonde.fr\",\n" +
            "              \"display_url\": \"lemonde.fr\",\n" +
            "              \"indices\": [\n" +
            "                0,\n" +
            "                22\n" +
            "              ]\n" +
            "            }\n" +
            "          ]\n" +
            "        },\n" +
            "        \"description\": {\n" +
            "          \"urls\": []\n" +
            "        }\n" +
            "      },\n" +
            "      \"protected\": false,\n" +
            "      \"followers_count\": 1596359,\n" +
            "      \"friends_count\": 226,\n" +
            "      \"listed_count\": 19776,\n" +
            "      \"created_at\": \"Mon Mar 16 18:44:51 +0000 2009\",\n" +
            "      \"favourites_count\": 107,\n" +
            "      \"utc_offset\": 3600,\n" +
            "      \"time_zone\": \"Paris\",\n" +
            "      \"geo_enabled\": false,\n" +
            "      \"verified\": true,\n" +
            "      \"statuses_count\": 82335,\n" +
            "      \"lang\": \"fr\",\n" +
            "      \"contributors_enabled\": false,\n" +
            "      \"is_translator\": false,\n" +
            "      \"profile_background_color\": \"DDE1EA\",\n" +
            "      \"profile_background_image_url\": \"http://a0.twimg.com/profile_background_images/255939997/twitter_lmfr.jpg\",\n" +
            "      \"profile_background_image_url_https\": \"https://si0.twimg.com/profile_background_images/255939997/twitter_lmfr.jpg\",\n" +
            "      \"profile_background_tile\": true,\n" +
            "      \"profile_image_url\": \"http://a0.twimg.com/profile_images/1430438688/IconeTwClassique_normal.jpg\",\n" +
            "      \"profile_image_url_https\": \"https://si0.twimg.com/profile_images/1430438688/IconeTwClassique_normal.jpg\",\n" +
            "      \"profile_link_color\": \"50B6CF\",\n" +
            "      \"profile_sidebar_border_color\": \"131316\",\n" +
            "      \"profile_sidebar_fill_color\": \"131316\",\n" +
            "      \"profile_text_color\": \"3292A8\",\n" +
            "      \"profile_use_background_image\": true,\n" +
            "      \"default_profile\": false,\n" +
            "      \"default_profile_image\": false,\n" +
            "      \"following\": true,\n" +
            "      \"follow_request_sent\": null,\n" +
            "      \"notifications\": null\n" +
            "    },\n" +
            "    \"geo\": null,\n" +
            "    \"coordinates\": null,\n" +
            "    \"place\": null,\n" +
            "    \"contributors\": null,\n" +
            "    \"retweet_count\": 30,\n" +
            "    \"favorite_count\": 4,\n" +
            "    \"entities\": {\n" +
            "      \"hashtags\": [\n" +
            "        {\n" +
            "          \"text\": \"Qatar\",\n" +
            "          \"indices\": [\n" +
            "            49,\n" +
            "            55\n" +
            "          ]\n" +
            "        }\n" +
            "      ],\n" +
            "      \"symbols\": [],\n" +
            "      \"urls\": [\n" +
            "        {\n" +
            "          \"url\": \"http://t.co/Vfp6RrjsSJ\",\n" +
            "          \"expanded_url\": \"http://lemde.fr/14UzYRC\",\n" +
            "          \"display_url\": \"lemde.fr/14UzYRC\",\n" +
            "          \"indices\": [\n" +
            "            56,\n" +
            "            78\n" +
            "          ]\n" +
            "        }\n" +
            "      ],\n" +
            "      \"user_mentions\": []\n" +
            "    },\n" +
            "    \"favorited\": false,\n" +
            "    \"retweeted\": false,\n" +
            "    \"possibly_sensitive\": false,\n" +
            "    \"lang\": \"fr\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"created_at\": \"Tue Jun 25 07:18:30 +0000 2013\",\n" +
            "    \"id\": 349426357484269600,\n" +
            "    \"id_str\": \"349426357484269570\",\n" +
            "    \"text\": \"Michel-Edouard Leclerc: \\\"les corporations font pression sur l'Etat\\\" http://t.co/bBBunpUI7a\",\n" +
            "    \"source\": \"<a href=\\\"http://twitter.com/tweetbutton\\\" rel=\\\"nofollow\\\">Tweet Button</a>\",\n" +
            "    \"truncated\": false,\n" +
            "    \"in_reply_to_status_id\": null,\n" +
            "    \"in_reply_to_status_id_str\": null,\n" +
            "    \"in_reply_to_user_id\": null,\n" +
            "    \"in_reply_to_user_id_str\": null,\n" +
            "    \"in_reply_to_screen_name\": null,\n" +
            "    \"user\": {\n" +
            "      \"id\": 140776364,\n" +
            "      \"id_str\": \"140776364\",\n" +
            "      \"name\": \"BFM Business\",\n" +
            "      \"screen_name\": \"bfmbusiness\",\n" +
            "      \"location\": \"\",\n" +
            "      \"description\": \"Compte officiel de BFM Business\",\n" +
            "      \"url\": \"http://t.co/JBiTL29ifZ\",\n" +
            "      \"entities\": {\n" +
            "        \"url\": {\n" +
            "          \"urls\": [\n" +
            "            {\n" +
            "              \"url\": \"http://t.co/JBiTL29ifZ\",\n" +
            "              \"expanded_url\": \"http://www.bfmbusiness.com\",\n" +
            "              \"display_url\": \"bfmbusiness.com\",\n" +
            "              \"indices\": [\n" +
            "                0,\n" +
            "                22\n" +
            "              ]\n" +
            "            }\n" +
            "          ]\n" +
            "        },\n" +
            "        \"description\": {\n" +
            "          \"urls\": []\n" +
            "        }\n" +
            "      },\n" +
            "      \"protected\": false,\n" +
            "      \"followers_count\": 27619,\n" +
            "      \"friends_count\": 353,\n" +
            "      \"listed_count\": 662,\n" +
            "      \"created_at\": \"Thu May 06 10:21:56 +0000 2010\",\n" +
            "      \"favourites_count\": 0,\n" +
            "      \"utc_offset\": 3600,\n" +
            "      \"time_zone\": \"Paris\",\n" +
            "      \"geo_enabled\": false,\n" +
            "      \"verified\": false,\n" +
            "      \"statuses_count\": 6606,\n" +
            "      \"lang\": \"fr\",\n" +
            "      \"contributors_enabled\": false,\n" +
            "      \"is_translator\": false,\n" +
            "      \"profile_background_color\": \"C0DEED\",\n" +
            "      \"profile_background_image_url\": \"http://a0.twimg.com/images/themes/theme1/bg.png\",\n" +
            "      \"profile_background_image_url_https\": \"https://si0.twimg.com/images/themes/theme1/bg.png\",\n" +
            "      \"profile_background_tile\": false,\n" +
            "      \"profile_image_url\": \"http://a0.twimg.com/profile_images/1277958396/icon-403747057-175x175_normal.jpg\",\n" +
            "      \"profile_image_url_https\": \"https://si0.twimg.com/profile_images/1277958396/icon-403747057-175x175_normal.jpg\",\n" +
            "      \"profile_link_color\": \"0084B4\",\n" +
            "      \"profile_sidebar_border_color\": \"C0DEED\",\n" +
            "      \"profile_sidebar_fill_color\": \"DDEEF6\",\n" +
            "      \"profile_text_color\": \"333333\",\n" +
            "      \"profile_use_background_image\": true,\n" +
            "      \"default_profile\": true,\n" +
            "      \"default_profile_image\": false,\n" +
            "      \"following\": true,\n" +
            "      \"follow_request_sent\": null,\n" +
            "      \"notifications\": null\n" +
            "    },\n" +
            "    \"geo\": null,\n" +
            "    \"coordinates\": null,\n" +
            "    \"place\": null,\n" +
            "    \"contributors\": null,\n" +
            "    \"retweet_count\": 6,\n" +
            "    \"favorite_count\": 4,\n" +
            "    \"entities\": {\n" +
            "      \"hashtags\": [],\n" +
            "      \"symbols\": [],\n" +
            "      \"urls\": [\n" +
            "        {\n" +
            "          \"url\": \"http://t.co/bBBunpUI7a\",\n" +
            "          \"expanded_url\": \"http://www.bfmtv.com/economie/michel-edouard-leclerc-les-corporations-font-pression-letat-545306.html\",\n" +
            "          \"display_url\": \"bfmtv.com/economie/miche…\",\n" +
            "          \"indices\": [\n" +
            "            68,\n" +
            "            90\n" +
            "          ]\n" +
            "        }\n" +
            "      ],\n" +
            "      \"user_mentions\": []\n" +
            "    },\n" +
            "    \"favorited\": false,\n" +
            "    \"retweeted\": false,\n" +
            "    \"possibly_sensitive\": false,\n" +
            "    \"lang\": \"fr\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"created_at\": \"Tue Jun 25 07:16:39 +0000 2013\",\n" +
            "    \"id\": 349425893216747500,\n" +
            "    \"id_str\": \"349425893216747520\",\n" +
            "    \"text\": \"L'écrivain de science-fiction Richard Matheson (auteur de \\\"Je suis une légende\\\", sorti en 1954) est mort http://t.co/9klXwAgmnj\",\n" +
            "    \"source\": \"<a href=\\\"http://www.tweetdeck.com\\\" rel=\\\"nofollow\\\">TweetDeck</a>\",\n" +
            "    \"truncated\": false,\n" +
            "    \"in_reply_to_status_id\": null,\n" +
            "    \"in_reply_to_status_id_str\": null,\n" +
            "    \"in_reply_to_user_id\": null,\n" +
            "    \"in_reply_to_user_id_str\": null,\n" +
            "    \"in_reply_to_screen_name\": null,\n" +
            "    \"user\": {\n" +
            "      \"id\": 24744541,\n" +
            "      \"id_str\": \"24744541\",\n" +
            "      \"name\": \"Le Monde\",\n" +
            "      \"screen_name\": \"lemondefr\",\n" +
            "      \"location\": \"Paris\",\n" +
            "      \"description\": \"Bienvenue sur le fil d'actualité du Monde.fr.\",\n" +
            "      \"url\": \"http://t.co/FAAzNQ8yF3\",\n" +
            "      \"entities\": {\n" +
            "        \"url\": {\n" +
            "          \"urls\": [\n" +
            "            {\n" +
            "              \"url\": \"http://t.co/FAAzNQ8yF3\",\n" +
            "              \"expanded_url\": \"http://www.lemonde.fr\",\n" +
            "              \"display_url\": \"lemonde.fr\",\n" +
            "              \"indices\": [\n" +
            "                0,\n" +
            "                22\n" +
            "              ]\n" +
            "            }\n" +
            "          ]\n" +
            "        },\n" +
            "        \"description\": {\n" +
            "          \"urls\": []\n" +
            "        }\n" +
            "      },\n" +
            "      \"protected\": false,\n" +
            "      \"followers_count\": 1596359,\n" +
            "      \"friends_count\": 226,\n" +
            "      \"listed_count\": 19776,\n" +
            "      \"created_at\": \"Mon Mar 16 18:44:51 +0000 2009\",\n" +
            "      \"favourites_count\": 107,\n" +
            "      \"utc_offset\": 3600,\n" +
            "      \"time_zone\": \"Paris\",\n" +
            "      \"geo_enabled\": false,\n" +
            "      \"verified\": true,\n" +
            "      \"statuses_count\": 82335,\n" +
            "      \"lang\": \"fr\",\n" +
            "      \"contributors_enabled\": false,\n" +
            "      \"is_translator\": false,\n" +
            "      \"profile_background_color\": \"DDE1EA\",\n" +
            "      \"profile_background_image_url\": \"http://a0.twimg.com/profile_background_images/255939997/twitter_lmfr.jpg\",\n" +
            "      \"profile_background_image_url_https\": \"https://si0.twimg.com/profile_background_images/255939997/twitter_lmfr.jpg\",\n" +
            "      \"profile_background_tile\": true,\n" +
            "      \"profile_image_url\": \"http://a0.twimg.com/profile_images/1430438688/IconeTwClassique_normal.jpg\",\n" +
            "      \"profile_image_url_https\": \"https://si0.twimg.com/profile_images/1430438688/IconeTwClassique_normal.jpg\",\n" +
            "      \"profile_link_color\": \"50B6CF\",\n" +
            "      \"profile_sidebar_border_color\": \"131316\",\n" +
            "      \"profile_sidebar_fill_color\": \"131316\",\n" +
            "      \"profile_text_color\": \"3292A8\",\n" +
            "      \"profile_use_background_image\": true,\n" +
            "      \"default_profile\": false,\n" +
            "      \"default_profile_image\": false,\n" +
            "      \"following\": true,\n" +
            "      \"follow_request_sent\": null,\n" +
            "      \"notifications\": null\n" +
            "    },\n" +
            "    \"geo\": null,\n" +
            "    \"coordinates\": null,\n" +
            "    \"place\": null,\n" +
            "    \"contributors\": null,\n" +
            "    \"retweet_count\": 99,\n" +
            "    \"favorite_count\": 12,\n" +
            "    \"entities\": {\n" +
            "      \"hashtags\": [],\n" +
            "      \"symbols\": [],\n" +
            "      \"urls\": [\n" +
            "        {\n" +
            "          \"url\": \"http://t.co/9klXwAgmnj\",\n" +
            "          \"expanded_url\": \"http://lemde.fr/14UzoTP\",\n" +
            "          \"display_url\": \"lemde.fr/14UzoTP\",\n" +
            "          \"indices\": [\n" +
            "            105,\n" +
            "            127\n" +
            "          ]\n" +
            "        }\n" +
            "      ],\n" +
            "      \"user_mentions\": []\n" +
            "    },\n" +
            "    \"favorited\": false,\n" +
            "    \"retweeted\": false,\n" +
            "    \"possibly_sensitive\": false,\n" +
            "    \"lang\": \"fr\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"created_at\": \"Tue Jun 25 07:12:33 +0000 2013\",\n" +
            "    \"id\": 349424859861880800,\n" +
            "    \"id_str\": \"349424859861880833\",\n" +
            "    \"text\": \"L'armée libanaise prend le contrôle du QG d'un cheikh sunnite radical http://t.co/7e41duGqq6\",\n" +
            "    \"source\": \"<a href=\\\"http://dlvr.it\\\" rel=\\\"nofollow\\\">dlvr.it</a>\",\n" +
            "    \"truncated\": false,\n" +
            "    \"in_reply_to_status_id\": null,\n" +
            "    \"in_reply_to_status_id_str\": null,\n" +
            "    \"in_reply_to_user_id\": null,\n" +
            "    \"in_reply_to_user_id_str\": null,\n" +
            "    \"in_reply_to_screen_name\": null,\n" +
            "    \"user\": {\n" +
            "      \"id\": 24744541,\n" +
            "      \"id_str\": \"24744541\",\n" +
            "      \"name\": \"Le Monde\",\n" +
            "      \"screen_name\": \"lemondefr\",\n" +
            "      \"location\": \"Paris\",\n" +
            "      \"description\": \"Bienvenue sur le fil d'actualité du Monde.fr.\",\n" +
            "      \"url\": \"http://t.co/FAAzNQ8yF3\",\n" +
            "      \"entities\": {\n" +
            "        \"url\": {\n" +
            "          \"urls\": [\n" +
            "            {\n" +
            "              \"url\": \"http://t.co/FAAzNQ8yF3\",\n" +
            "              \"expanded_url\": \"http://www.lemonde.fr\",\n" +
            "              \"display_url\": \"lemonde.fr\",\n" +
            "              \"indices\": [\n" +
            "                0,\n" +
            "                22\n" +
            "              ]\n" +
            "            }\n" +
            "          ]\n" +
            "        },\n" +
            "        \"description\": {\n" +
            "          \"urls\": []\n" +
            "        }\n" +
            "      },\n" +
            "      \"protected\": false,\n" +
            "      \"followers_count\": 1596359,\n" +
            "      \"friends_count\": 226,\n" +
            "      \"listed_count\": 19776,\n" +
            "      \"created_at\": \"Mon Mar 16 18:44:51 +0000 2009\",\n" +
            "      \"favourites_count\": 107,\n" +
            "      \"utc_offset\": 3600,\n" +
            "      \"time_zone\": \"Paris\",\n" +
            "      \"geo_enabled\": false,\n" +
            "      \"verified\": true,\n" +
            "      \"statuses_count\": 82335,\n" +
            "      \"lang\": \"fr\",\n" +
            "      \"contributors_enabled\": false,\n" +
            "      \"is_translator\": false,\n" +
            "      \"profile_background_color\": \"DDE1EA\",\n" +
            "      \"profile_background_image_url\": \"http://a0.twimg.com/profile_background_images/255939997/twitter_lmfr.jpg\",\n" +
            "      \"profile_background_image_url_https\": \"https://si0.twimg.com/profile_background_images/255939997/twitter_lmfr.jpg\",\n" +
            "      \"profile_background_tile\": true,\n" +
            "      \"profile_image_url\": \"http://a0.twimg.com/profile_images/1430438688/IconeTwClassique_normal.jpg\",\n" +
            "      \"profile_image_url_https\": \"https://si0.twimg.com/profile_images/1430438688/IconeTwClassique_normal.jpg\",\n" +
            "      \"profile_link_color\": \"50B6CF\",\n" +
            "      \"profile_sidebar_border_color\": \"131316\",\n" +
            "      \"profile_sidebar_fill_color\": \"131316\",\n" +
            "      \"profile_text_color\": \"3292A8\",\n" +
            "      \"profile_use_background_image\": true,\n" +
            "      \"default_profile\": false,\n" +
            "      \"default_profile_image\": false,\n" +
            "      \"following\": true,\n" +
            "      \"follow_request_sent\": null,\n" +
            "      \"notifications\": null\n" +
            "    },\n" +
            "    \"geo\": null,\n" +
            "    \"coordinates\": null,\n" +
            "    \"place\": null,\n" +
            "    \"contributors\": null,\n" +
            "    \"retweet_count\": 24,\n" +
            "    \"favorite_count\": 5,\n" +
            "    \"entities\": {\n" +
            "      \"hashtags\": [],\n" +
            "      \"symbols\": [],\n" +
            "      \"urls\": [\n" +
            "        {\n" +
            "          \"url\": \"http://t.co/7e41duGqq6\",\n" +
            "          \"expanded_url\": \"http://lemde.fr/1cgZscu\",\n" +
            "          \"display_url\": \"lemde.fr/1cgZscu\",\n" +
            "          \"indices\": [\n" +
            "            70,\n" +
            "            92\n" +
            "          ]\n" +
            "        }\n" +
            "      ],\n" +
            "      \"user_mentions\": []\n" +
            "    },\n" +
            "    \"favorited\": false,\n" +
            "    \"retweeted\": false,\n" +
            "    \"possibly_sensitive\": false,\n" +
            "    \"lang\": \"fr\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"created_at\": \"Tue Jun 25 07:06:52 +0000 2013\",\n" +
            "    \"id\": 349423431990779900,\n" +
            "    \"id_str\": \"349423431990779904\",\n" +
            "    \"text\": \"Free brade la Freebox Crystal à 1,99 euro/mois sur http://t.co/lHfoFbdBBd http://t.co/rIYRuFjSO9\",\n" +
            "    \"source\": \"web\",\n" +
            "    \"truncated\": false,\n" +
            "    \"in_reply_to_status_id\": null,\n" +
            "    \"in_reply_to_status_id_str\": null,\n" +
            "    \"in_reply_to_user_id\": null,\n" +
            "    \"in_reply_to_user_id_str\": null,\n" +
            "    \"in_reply_to_screen_name\": null,\n" +
            "    \"user\": {\n" +
            "      \"id\": 18239824,\n" +
            "      \"id_str\": \"18239824\",\n" +
            "      \"name\": \"Clubic\",\n" +
            "      \"screen_name\": \"Clubic\",\n" +
            "      \"location\": \"\",\n" +
            "      \"description\": \"Toute l’actu #hitech : #news, #dossiers et #tests. \\r\\nBonus : #bonplans + #livetweet + #teasing.\",\n" +
            "      \"url\": \"http://t.co/4ipsYKyA8k\",\n" +
            "      \"entities\": {\n" +
            "        \"url\": {\n" +
            "          \"urls\": [\n" +
            "            {\n" +
            "              \"url\": \"http://t.co/4ipsYKyA8k\",\n" +
            "              \"expanded_url\": \"http://www.clubic.com\",\n" +
            "              \"display_url\": \"clubic.com\",\n" +
            "              \"indices\": [\n" +
            "                0,\n" +
            "                22\n" +
            "              ]\n" +
            "            }\n" +
            "          ]\n" +
            "        },\n" +
            "        \"description\": {\n" +
            "          \"urls\": []\n" +
            "        }\n" +
            "      },\n" +
            "      \"protected\": false,\n" +
            "      \"followers_count\": 31919,\n" +
            "      \"friends_count\": 193,\n" +
            "      \"listed_count\": 1316,\n" +
            "      \"created_at\": \"Fri Dec 19 11:16:35 +0000 2008\",\n" +
            "      \"favourites_count\": 5,\n" +
            "      \"utc_offset\": 3600,\n" +
            "      \"time_zone\": \"Paris\",\n" +
            "      \"geo_enabled\": true,\n" +
            "      \"verified\": false,\n" +
            "      \"statuses_count\": 35251,\n" +
            "      \"lang\": \"fr\",\n" +
            "      \"contributors_enabled\": false,\n" +
            "      \"is_translator\": false,\n" +
            "      \"profile_background_color\": \"131516\",\n" +
            "      \"profile_background_image_url\": \"http://a0.twimg.com/profile_background_images/92688003/bg_home.jpg\",\n" +
            "      \"profile_background_image_url_https\": \"https://si0.twimg.com/profile_background_images/92688003/bg_home.jpg\",\n" +
            "      \"profile_background_tile\": true,\n" +
            "      \"profile_image_url\": \"http://a0.twimg.com/profile_images/1148040974/200-200_normal.png\",\n" +
            "      \"profile_image_url_https\": \"https://si0.twimg.com/profile_images/1148040974/200-200_normal.png\",\n" +
            "      \"profile_banner_url\": \"https://pbs.twimg.com/profile_banners/18239824/1347980725\",\n" +
            "      \"profile_link_color\": \"E1190D\",\n" +
            "      \"profile_sidebar_border_color\": \"000000\",\n" +
            "      \"profile_sidebar_fill_color\": \"FFFFFF\",\n" +
            "      \"profile_text_color\": \"000000\",\n" +
            "      \"profile_use_background_image\": true,\n" +
            "      \"default_profile\": false,\n" +
            "      \"default_profile_image\": false,\n" +
            "      \"following\": true,\n" +
            "      \"follow_request_sent\": null,\n" +
            "      \"notifications\": null\n" +
            "    },\n" +
            "    \"geo\": null,\n" +
            "    \"coordinates\": null,\n" +
            "    \"place\": null,\n" +
            "    \"contributors\": null,\n" +
            "    \"retweet_count\": 11,\n" +
            "    \"favorite_count\": 2,\n" +
            "    \"entities\": {\n" +
            "      \"hashtags\": [],\n" +
            "      \"symbols\": [],\n" +
            "      \"urls\": [\n" +
            "        {\n" +
            "          \"url\": \"http://t.co/lHfoFbdBBd\",\n" +
            "          \"expanded_url\": \"http://Vente-privee.com\",\n" +
            "          \"display_url\": \"Vente-privee.com\",\n" +
            "          \"indices\": [\n" +
            "            51,\n" +
            "            73\n" +
            "          ]\n" +
            "        },\n" +
            "        {\n" +
            "          \"url\": \"http://t.co/rIYRuFjSO9\",\n" +
            "          \"expanded_url\": \"http://bit.ly/1ce987u\",\n" +
            "          \"display_url\": \"bit.ly/1ce987u\",\n" +
            "          \"indices\": [\n" +
            "            74,\n" +
            "            96\n" +
            "          ]\n" +
            "        }\n" +
            "      ],\n" +
            "      \"user_mentions\": []\n" +
            "    },\n" +
            "    \"favorited\": false,\n" +
            "    \"retweeted\": false,\n" +
            "    \"possibly_sensitive\": false,\n" +
            "    \"lang\": \"fr\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"created_at\": \"Wed Jun 19 19:31:43 +0000 2013\",\n" +
            "    \"id\": 347436551153791000,\n" +
            "    \"id_str\": \"347436551153790977\",\n" +
            "    \"text\": \"Microsoft apporte à son réseau social Socl une nouvelle interface, un outil de traitement des GIF animés et bi... http://t.co/koWuE7NIXM\",\n" +
            "    \"source\": \"<a href=\\\"http://twitterfeed.com\\\" rel=\\\"nofollow\\\">twitterfeed</a>\",\n" +
            "    \"truncated\": false,\n" +
            "    \"in_reply_to_status_id\": null,\n" +
            "    \"in_reply_to_status_id_str\": null,\n" +
            "    \"in_reply_to_user_id\": null,\n" +
            "    \"in_reply_to_user_id_str\": null,\n" +
            "    \"in_reply_to_screen_name\": null,\n" +
            "    \"user\": {\n" +
            "      \"id\": 55410398,\n" +
            "      \"id_str\": \"55410398\",\n" +
            "      \"name\": \"Developpez.com\",\n" +
            "      \"screen_name\": \"Developpez\",\n" +
            "      \"location\": \"Francophonie internationale\",\n" +
            "      \"description\": \"Club des professionnels en informatique\",\n" +
            "      \"url\": \"http://t.co/Opg73fK1tZ\",\n" +
            "      \"entities\": {\n" +
            "        \"url\": {\n" +
            "          \"urls\": [\n" +
            "            {\n" +
            "              \"url\": \"http://t.co/Opg73fK1tZ\",\n" +
            "              \"expanded_url\": \"http://www.developpez.com/\",\n" +
            "              \"display_url\": \"developpez.com\",\n" +
            "              \"indices\": [\n" +
            "                0,\n" +
            "                22\n" +
            "              ]\n" +
            "            }\n" +
            "          ]\n" +
            "        },\n" +
            "        \"description\": {\n" +
            "          \"urls\": []\n" +
            "        }\n" +
            "      },\n" +
            "      \"protected\": false,\n" +
            "      \"followers_count\": 12935,\n" +
            "      \"friends_count\": 63,\n" +
            "      \"listed_count\": 643,\n" +
            "      \"created_at\": \"Fri Jul 10 00:15:04 +0000 2009\",\n" +
            "      \"favourites_count\": 0,\n" +
            "      \"utc_offset\": 3600,\n" +
            "      \"time_zone\": \"Paris\",\n" +
            "      \"geo_enabled\": false,\n" +
            "      \"verified\": false,\n" +
            "      \"statuses_count\": 10606,\n" +
            "      \"lang\": \"fr\",\n" +
            "      \"contributors_enabled\": false,\n" +
            "      \"is_translator\": false,\n" +
            "      \"profile_background_color\": \"DFE2E2\",\n" +
            "      \"profile_background_image_url\": \"http://a0.twimg.com/profile_background_images/89076960/x06d6b0ed30705bbadccc79da87feda8.jpg\",\n" +
            "      \"profile_background_image_url_https\": \"https://si0.twimg.com/profile_background_images/89076960/x06d6b0ed30705bbadccc79da87feda8.jpg\",\n" +
            "      \"profile_background_tile\": false,\n" +
            "      \"profile_image_url\": \"http://a0.twimg.com/profile_images/317934580/TWITTER_normal.png\",\n" +
            "      \"profile_image_url_https\": \"https://si0.twimg.com/profile_images/317934580/TWITTER_normal.png\",\n" +
            "      \"profile_link_color\": \"0000FF\",\n" +
            "      \"profile_sidebar_border_color\": \"87BC44\",\n" +
            "      \"profile_sidebar_fill_color\": \"E0FF92\",\n" +
            "      \"profile_text_color\": \"000000\",\n" +
            "      \"profile_use_background_image\": true,\n" +
            "      \"default_profile\": false,\n" +
            "      \"default_profile_image\": false,\n" +
            "      \"following\": true,\n" +
            "      \"follow_request_sent\": null,\n" +
            "      \"notifications\": null\n" +
            "    },\n" +
            "    \"geo\": null,\n" +
            "    \"coordinates\": null,\n" +
            "    \"place\": null,\n" +
            "    \"contributors\": null,\n" +
            "    \"retweet_count\": 0,\n" +
            "    \"favorite_count\": 0,\n" +
            "    \"entities\": {\n" +
            "      \"hashtags\": [],\n" +
            "      \"symbols\": [],\n" +
            "      \"urls\": [\n" +
            "        {\n" +
            "          \"url\": \"http://t.co/koWuE7NIXM\",\n" +
            "          \"expanded_url\": \"http://bit.ly/16L0gFO\",\n" +
            "          \"display_url\": \"bit.ly/16L0gFO\",\n" +
            "          \"indices\": [\n" +
            "            114,\n" +
            "            136\n" +
            "          ]\n" +
            "        }\n" +
            "      ],\n" +
            "      \"user_mentions\": []\n" +
            "    },\n" +
            "    \"favorited\": false,\n" +
            "    \"retweeted\": false,\n" +
            "    \"possibly_sensitive\": false,\n" +
            "    \"lang\": \"fr\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"created_at\": \"Wed Jun 19 19:30:05 +0000 2013\",\n" +
            "    \"id\": 347436141366087700,\n" +
            "    \"id_str\": \"347436141366087680\",\n" +
            "    \"text\": \"Photoshop CC : les nouveautés en images http://t.co/nfegD8OOIW #rappel\",\n" +
            "    \"source\": \"<a href=\\\"http://bufferapp.com\\\" rel=\\\"nofollow\\\">Buffer</a>\",\n" +
            "    \"truncated\": false,\n" +
            "    \"in_reply_to_status_id\": null,\n" +
            "    \"in_reply_to_status_id_str\": null,\n" +
            "    \"in_reply_to_user_id\": null,\n" +
            "    \"in_reply_to_user_id_str\": null,\n" +
            "    \"in_reply_to_screen_name\": null,\n" +
            "    \"user\": {\n" +
            "      \"id\": 18239824,\n" +
            "      \"id_str\": \"18239824\",\n" +
            "      \"name\": \"Clubic\",\n" +
            "      \"screen_name\": \"Clubic\",\n" +
            "      \"location\": \"\",\n" +
            "      \"description\": \"Toute l’actu #hitech : #news, #dossiers et #tests. \\r\\nBonus : #bonplans + #livetweet + #teasing.\",\n" +
            "      \"url\": \"http://t.co/4ipsYKyA8k\",\n" +
            "      \"entities\": {\n" +
            "        \"url\": {\n" +
            "          \"urls\": [\n" +
            "            {\n" +
            "              \"url\": \"http://t.co/4ipsYKyA8k\",\n" +
            "              \"expanded_url\": \"http://www.clubic.com\",\n" +
            "              \"display_url\": \"clubic.com\",\n" +
            "              \"indices\": [\n" +
            "                0,\n" +
            "                22\n" +
            "              ]\n" +
            "            }\n" +
            "          ]\n" +
            "        },\n" +
            "        \"description\": {\n" +
            "          \"urls\": []\n" +
            "        }\n" +
            "      },\n" +
            "      \"protected\": false,\n" +
            "      \"followers_count\": 31658,\n" +
            "      \"friends_count\": 193,\n" +
            "      \"listed_count\": 1312,\n" +
            "      \"created_at\": \"Fri Dec 19 11:16:35 +0000 2008\",\n" +
            "      \"favourites_count\": 5,\n" +
            "      \"utc_offset\": 3600,\n" +
            "      \"time_zone\": \"Paris\",\n" +
            "      \"geo_enabled\": true,\n" +
            "      \"verified\": false,\n" +
            "      \"statuses_count\": 35052,\n" +
            "      \"lang\": \"fr\",\n" +
            "      \"contributors_enabled\": false,\n" +
            "      \"is_translator\": false,\n" +
            "      \"profile_background_color\": \"131516\",\n" +
            "      \"profile_background_image_url\": \"http://a0.twimg.com/profile_background_images/92688003/bg_home.jpg\",\n" +
            "      \"profile_background_image_url_https\": \"https://si0.twimg.com/profile_background_images/92688003/bg_home.jpg\",\n" +
            "      \"profile_background_tile\": true,\n" +
            "      \"profile_image_url\": \"http://a0.twimg.com/profile_images/1148040974/200-200_normal.png\",\n" +
            "      \"profile_image_url_https\": \"https://si0.twimg.com/profile_images/1148040974/200-200_normal.png\",\n" +
            "      \"profile_banner_url\": \"https://pbs.twimg.com/profile_banners/18239824/1347980725\",\n" +
            "      \"profile_link_color\": \"E1190D\",\n" +
            "      \"profile_sidebar_border_color\": \"000000\",\n" +
            "      \"profile_sidebar_fill_color\": \"FFFFFF\",\n" +
            "      \"profile_text_color\": \"000000\",\n" +
            "      \"profile_use_background_image\": true,\n" +
            "      \"default_profile\": false,\n" +
            "      \"default_profile_image\": false,\n" +
            "      \"following\": true,\n" +
            "      \"follow_request_sent\": null,\n" +
            "      \"notifications\": null\n" +
            "    },\n" +
            "    \"geo\": null,\n" +
            "    \"coordinates\": null,\n" +
            "    \"place\": null,\n" +
            "    \"contributors\": null,\n" +
            "    \"retweet_count\": 1,\n" +
            "    \"favorite_count\": 0,\n" +
            "    \"entities\": {\n" +
            "      \"hashtags\": [\n" +
            "        {\n" +
            "          \"text\": \"rappel\",\n" +
            "          \"indices\": [\n" +
            "            63,\n" +
            "            70\n" +
            "          ]\n" +
            "        }\n" +
            "      ],\n" +
            "      \"symbols\": [],\n" +
            "      \"urls\": [\n" +
            "        {\n" +
            "          \"url\": \"http://t.co/nfegD8OOIW\",\n" +
            "          \"expanded_url\": \"http://bit.ly/10wh6sN\",\n" +
            "          \"display_url\": \"bit.ly/10wh6sN\",\n" +
            "          \"indices\": [\n" +
            "            40,\n" +
            "            62\n" +
            "          ]\n" +
            "        }\n" +
            "      ],\n" +
            "      \"user_mentions\": []\n" +
            "    },\n" +
            "    \"favorited\": false,\n" +
            "    \"retweeted\": false,\n" +
            "    \"possibly_sensitive\": false,\n" +
            "    \"lang\": \"fr\"\n" +
            "  }\n" +
            "]";

    private JsonFactory mJSONFactory = new JsonFactory();
    private DateTimeFormatter mDateFormat = DateTimeFormat.forPattern("EEE MMM d HH:mm:ss z yyyy").withZone(DateTimeZone.UTC);

//    public TweetPage parseTweetPage(InputStream lInputStream) throws TweetAccessException {
//        JsonParser lParser = null;
//        try {
//            lParser = mJSONFactory.createParser(lInputStream);
//            return parseTweetPage(pPageSize, lParser);
//        } catch (IOException eIOException) {
//            throw TweetAccessException.from(eIOException);
//        } finally {
//            try {
//                if (lParser != null) lParser.close();
//            } catch (IOException eIOException) {
//                eIOException.printStackTrace();
//            }
//        }
//    }

    @Override
    public List<Tweet> findTweets() {
        JsonParser parser = null;
        try {
            parser = mJSONFactory.createParser(JSON_FILE);
            if (parser.nextToken() != JsonToken.START_ARRAY) throw new IOException();
            List<Tweet> tweets = new ArrayList<Tweet>(/*TODO Capacity*/);

            boolean finished = false;
            while (!finished) {
                switch (parser.nextToken()) {
                    case START_OBJECT:
                        Tweet tweet = Tweet__JsonHelper.parseFromJson(parser);
                        tweets.add(tweet);
                        break;
                    case END_ARRAY:
                        finished = true;
                        break;
                    case NOT_AVAILABLE:
                        throw new IOException();
                    default:
                        break;
                }
            }
            return tweets;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
