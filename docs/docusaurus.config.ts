import type {Config} from '@docusaurus/types';
import type * as Preset from '@docusaurus/preset-classic';
import {themes} from "prism-react-renderer";

const lightTheme = themes.oceanicNext;
const darkTheme = themes.oceanicNext;

const config: Config = {
    title: 'Inventory Sorter - Sorts your Minecraft inventories with the click of a button!',
    titleDelimiter: '–',
    favicon: 'img/brush_button.png',
    url: 'https://invsort.meza.gg',
    baseUrl: '/',
    trailingSlash: false,

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
        metadata: [
            {name: 'description', content: 'Inventory Sorter is a lightweight Minecraft mod that adds intuitive, customizable inventory sorting for both modded and vanilla clients. Supports buttons, keybinds, double-click sorting, and server control.'},
            {name: 'keywords', content: 'Minecraft mod, inventory sorting, Fabric mod, Forge mod, server-side, client-side, keybind sorting, Minecraft QoL mod, inventory management, double-click sort, modded Minecraft, sort items'},
            {name: 'og:title', content: 'Inventory Sorter Mod for Minecraft'},
            {name: 'og:description', content: 'Powerful and flexible inventory sorting for Minecraft. Works on both vanilla and modded clients with minimal configuration.'},
            {name: 'og:type', content: 'website'},
            {name: 'og:url', content: 'https://invsort.meza.gg'},
            {name: 'og:image', content: 'https://invsort.meza.gg/img/brush_button.png'},
            {name: 'twitter:card', content: 'summary_large_image'},
            {name: 'twitter:site', content: '@houseofmeza'},
            {name: 'twitter:creator', content: '@houseofmeza'},
            {name: 'twitter:image', content: 'https://invsort.meza.gg/img/brush_button.png'},
            {name: 'twitter:title', content: 'Inventory Sorter Mod for Minecraft'},
            {name: 'twitter:description', content: 'Sort any inventory in Minecraft with ease. Supports buttons, keybinds, and server-configurable rules.'},
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
                src: "img/brush_button.png",
            },
            items:[
                {
                    label: '❤️ Sponsor',
                    position: 'right',
                    href: 'https://github.com/sponsors/meza',
                    target: '_blank'
                },
                {
                    position: 'right',
                    className: 'github-link',
                    'aria-label': 'GitHub repository',
                    href: 'https://github.com/KyrptonaughtMC/Inventory-Sorter',
                    target: '_blank'
                }
            ]
        },
        footer: {
            style: "dark",
            logo: {
                alt: "Inventory Sorter Logo",
                src: "img/brush_button.png",
                width: 80,
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
            copyright: `Copyright © 2025, under the MIT license.<br/>Built with Docusaurus.`,
        },
        prism: {
            theme: lightTheme,
            darkTheme: darkTheme,
            additionalLanguages: ["java", "gradle", "kotlin", "json", "json5"],
        }
    } satisfies Preset.ThemeConfig
};

export default config;
