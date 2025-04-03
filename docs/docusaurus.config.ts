import type {Config} from '@docusaurus/types';
import type * as Preset from '@docusaurus/preset-classic';
import {themes} from "prism-react-renderer";

const lightTheme = themes.oceanicNext;
const darkTheme = themes.oceanicNext;

const config: Config = {
    title: 'Inventory Sorter - Sorts your Minecraft inventories with the click of a button!',
    titleDelimiter: '–',
    favicon: 'img/icon.png',
    url: 'https://invsort.meza.gg',
    baseUrl: '/',
    trailingSlash: false,

    /* Your site config here */

    i18n: {
        defaultLocale: "en",
        locales: ["en"],
    },
    presets: [
        [
            '@docusaurus/preset-classic',
            {
                theme: {
                    customCss: ['./src/css/custom.css']
                },
                docs: {
                    sidebarPath: './sidebars.ts',
                    routeBasePath: '/'
                },
                blog: false,
            } satisfies Preset.Options,
        ],
    ],
    headTags: [
        {
            tagName: 'link',
            attributes: {
                rel: 'canonical',
                href: 'https://invsort.meza.gg',
            }
        },
        {
            tagName: 'script',
            attributes: {
                type: 'application/ld+json',
            },
            innerHTML: JSON.stringify({
                "@context": "https://schema.org",
                "@type": "SoftwareApplication",
                "name": "Inventory Sorter",
                "description": "Inventory Sorter - Sorts your Minecraft inventories with the click of a button!",
                "applicationCategory": "DeveloperTool",
                "operatingSystem": "Cross-platform",
                "url": "https://invsort.meza.gg/",
                "author": {
                    "@type": "Person",
                    "name": "Meza"
                }
            }),
        }
    ],
    themeConfig: {
        algolia: {
            appId: '9CN68M6PTQ',
            apiKey: 'c04256a693ddd8deb649fcc774ced671',
            indexName: 'Inventory Sorter-meza',
            insights: true,
            contextualSearch: true
        },
        metadata: [
            {name: 'description', content: 'Inventory Sorter is a Gradle plugin that streamlines multi-loader, multi-version Minecraft mod development. Build Fabric, Forge, and NeoForge mods in a single codebase and simplify releases to Modrinth and CurseForge.'},
            {name: 'keywords', content: 'Minecraft modding, Gradle plugin, Fabric mod, Forge mod, NeoForge, multi-loader modding, Stonecutter, Architectury, Modrinth, CurseForge'},
            {name: 'og:title', content: 'Inventory Sorter – Simplify Multi-Loader, Multi-Version Minecraft Mod Development'},
            {name: 'og:description', content: 'Inventory Sorter is a Gradle plugin that streamlines multi-loader, multi-version Minecraft mod development. Build Fabric, Forge, and NeoForge mods in a single codebase and simplify releases to Modrinth and CurseForge.'},
            {name: 'og:type', content: 'website'},
            {name: 'og:url', content: 'https://invsort.meza.gg'},
            {name: 'og:image', content: 'https://invsort.meza.gg/img/Inventory Sorter@0.5x.png'},
            {name: 'twitter:card', content: 'summary_large_image'},
            {name: 'twitter:site', content: '@houseofmeza'},
            {name: 'twitter:creator', content: '@houseofmeza'},
            {name: 'twitter:image', content: 'https://invsort.meza.gg/img/Inventory Sorter@0.5x.png'},
            {name: 'twitter:title', content: 'Inventory Sorter – Simplify Multi-Loader, Multi-Version Minecraft Mod Development'},
            {name: 'twitter:description', content: 'Inventory Sorter is a Gradle plugin that streamlines multi-loader, multi-version Minecraft mod development. Build Fabric, Forge, and NeoForge mods in a single codebase and simplify releases to Modrinth and CurseForge.'}
        ],
        announcementBar: {
            isCloseable: true,
            id: 'star',
            backgroundColor: '#5cb85c',
            content: '⭐️ If you like Inventory Sorter, give it a star on <a target="_blank" rel="noopener noreferrer" href="https://github.com/KyrptonaughtMC/Inventory-Sorter">GitHub</a>! ⭐️',
        },
        colorMode: {
            defaultMode: 'dark',
            disableSwitch: false,
            respectPrefersColorScheme: true,
        },
        navbar: {
            title: "Inventory Sorter",
            logo: {
                alt: "Inventory Sorter Logo",
                src: "img/icon.png",
            },
            items:[
                {
                    position: 'right',
                    className: 'github-link',
                    'aria-label': 'GitHub repository',
                    href: 'https://github.com/meza/Inventory Sorter'
                }
            ]
        },
        footer: {
            style: "dark",
            logo: {
                alt: "Inventory Sorter Logo",
                src: "img/icon.png",
            },
            links: [
                {
                    title: "Docs",
                    items: [
                        {
                            to: "/",
                            label: "Documentation",
                        },
                        {
                            to: "/contributing",
                            label: "Contributing to the Documentation"
                        }
                    ],
                },
                {
                    title: "Links",
                    items: [
                        {
                            label: "Discord",
                            href: "https://discord.gg/dvg3tcQCPW",
                        },
                        {
                            label: "Main Website",
                            href: "https://invsort.meza.gg/",
                        },
                        {
                            label: "GitHub",
                            href: "https://github.com/KyrptonaughtMC/Inventory-Sorter",
                        },
                    ],
                },
            ],
            copyright: `Copyright © 2025, under the GPL-3.0 license. Built with Docusaurus.`,
        },
        prism: {
            theme: lightTheme,
            darkTheme: darkTheme,
            additionalLanguages: ["java", "gradle", "kotlin", "json", "json5"],
        }
    } satisfies Preset.ThemeConfig
};

export default config;
