import { defineConfig } from 'vitepress'
import { withMermaid } from 'vitepress-plugin-mermaid'

export default withMermaid(defineConfig({
  cleanUrls: true,
  sitemap: {
    hostname: 'https://rwx-docs.netlify.app'
  },
  themeConfig: {
    logo: { src: '/logo.svg', alt: 'RWX Logo' },
    search: {
      provider: 'local'
    }
  },
  // Route rewrite: serve Chinese sources from site root.
  rewrites: {
    'zh/:path*': ':path*'
  },
  locales: {
    root: {
      /*   label: 'English',
         lang: 'en-US',
         title: 'RWX',
         description: 'Open-source cross-platform RTS game',
         head: [
           ['link', { rel: 'icon', href: '/logo.svg', type: 'image/svg+xml', sizes: 'any' }]
         ],
         themeConfig: {
           nav: [
             { text: 'Home', link: '/' },
             { text: 'Tutorial', link: '/tutorial/getting-started' },
             { text: 'Modding', link: '/modding/introduction' },
             { text: 'Community', link: '/community' }
           ],
           sidebar: {
             '/tutorial/': [
               {
                 text: 'Tutorial',
                 items: [
                   { text: 'Getting Started', link: '/tutorial/getting-started' },
                   { text: 'P2P Multiplayer', link: '/tutorial/p2p' },
                   { text: 'Area Control', link: '/tutorial/area-control' },
                   { text: 'Linked Maps', link: '/tutorial/linked-maps' }
                 ]
               }
             ],
             '/modding/': [
               {
                 text: 'Modding',
                 items: [
                   { text: 'Introduction', link: '/modding/introduction' },
                   {
                     text: 'JVM Mods',
                     collapsed: false,
                     items: [
                       { text: 'Overview and Lifecycle', link: '/modding/jvmMod' },
                       { text: 'Unit Commands', link: '/modding/jvm-unit-commands' },
                       { text: 'Content API', link: '/modding/jvm-content' },
                       { text: 'Rendering and Effects', link: '/modding/jvm-rendering' },
                       { text: 'Game Runtime', link: '/modding/jvm-runtime' },
                        { text: 'Compose UI and HUD', link: '/modding/jvm-ui' },
                       {text: 'AI', link: '/modding/jvm-ai'}
                     ]
                   }
                 ]
               }
             ]
           },
           socialLinks: [
             { icon: 'github', link: 'https://github.com/yomi2539/RWX' }
           ],
           footer: {
             message: 'Released under AGPL-3.0',
             copyright: `Copyright © ${new Date().getFullYear()} RWX Team`
           },
           search: {
             provider: 'local',
             options: {
               translations: {
                 button: { buttonText: 'Search docs', buttonAriaLabel: 'Search docs' },
                 modal: {
                   noResultsText: 'No results found',
                   resetButtonTitle: 'Reset',
                   footer: { selectText: 'Select', navigateText: 'Navigate', closeText: 'Close' }
                 }
               }
             }
           }
         }
       },
       zh: {*/
      label: '简体中文',
      lang: 'zh-CN',
      title: 'RWX',
      description: '开源跨平台 RTS 游戏',
      head: [
        ['link', { rel: 'icon', href: '/logo.svg', type: 'image/svg+xml', sizes: 'any' }]
      ],
      themeConfig: {
        nav: [
          {text: '首页', link: '/'},
          {text: '教程', link: '/tutorial/getting-started'},
          {text: '模组开发', link: '/modding/introduction'},
          {text: '社区', link: '/community'}
        ],
        sidebar: {
          '/tutorial/': [
            {
              text: '教程',
              items: [
                {text: '快速开始', link: '/tutorial/getting-started'},
                {text: 'P2P 联机', link: '/tutorial/p2p'},
                {text: '区域控制', link: '/tutorial/area-control'},
                {text: '地图联通', link: '/tutorial/linked-maps'}
              ]
            }
          ],
          '/modding/': [
            {
              text: '模组开发',
              items: [
                {text: '模组介绍', link: '/modding/introduction'},
                {
                  text: 'JVM 模组',
                  collapsed: false,
                  items: [
                    {text: '总览与生命周期', link: '/modding/jvmMod'},
                    {text: '单位命令', link: '/modding/jvm-unit-commands'},
                    {text: '内容 API', link: '/modding/jvm-content'},
                    {text: '渲染与效果', link: '/modding/jvm-rendering'},
                    {text: '游戏运行时', link: '/modding/jvm-runtime'},
                    {text: 'Compose UI 与 HUD', link: '/modding/jvm-ui'},
                    {text: 'AI', link: '/modding/jvm-ai'}
                  ]
                }
              ]
            }
          ]
        },
        socialLinks: [
          {icon: 'github', link: 'https://github.com/yomi2539/RWX'}
        ],
        footer: {
          message: '使用 AGPL-3.0 协议发布',
          copyright: `Copyright © ${new Date().getFullYear()} RWX Team`
        },
        search: {
          provider: 'local',
          options: {
            translations: {
              button: { buttonText: '搜索文档', buttonAriaLabel: '搜索文档' },
              modal: {
                noResultsText: '没有找到相关结果',
                resetButtonTitle: '清除查询',
                footer: { selectText: '选择', navigateText: '切换', closeText: '关闭' }
              }
            }
          }
        }
      }
    }
  }
}))
