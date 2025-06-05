package com.jfirer.baseutil;

import com.microsoft.playwright.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedTransferQueue;

public class PlaywrightUtil
{
    protected Queue<BrowserContext> contextQueue = new LinkedTransferQueue<>();
    protected Playwright            playwright;
    protected Browser               browser;

    public PlaywrightUtil(boolean headless, int initSize)
    {
        playwright = Playwright.create();
        browser    = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(headless)
                                                                                 .setArgs(List.of("--incognito"))
        );
        List<BrowserContext> list = new LinkedList<>();
        for (int i = 0; i < initSize; i++)
        {
            list.add(createContext());
        }
        for (BrowserContext context : list)
        {
            returnContext(context);
        }
    }

    public void returnContext(BrowserContext context)
    {
        clearContext(context);
        contextQueue.offer(context);
    }

    public BrowserContext getContext()
    {
        BrowserContext context = contextQueue.poll();
        if (context == null)
        {
            context = createContext();
        }
        else
        {
            ;
        }
        return context;
    }

    private BrowserContext createContext()
    {
        BrowserContext context = browser.newContext(new Browser.NewContextOptions()//
                                                                                   .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36")//
                                                                                   .setBypassCSP(true)//
                                                                                   // 设置视口大小
                                                                                   .setViewportSize(1920, 1080)
                                                                                   // 设置语言
                                                                                   .setLocale("en-US")//
                                                                                   .setAcceptDownloads(true)//
                                                                                   .setJavaScriptEnabled(true)
        );
//         Disable automation indicators
        context.addInitScript("() => { Object.defineProperty(navigator, 'webdriver', { get: () => false }); }");
        context.setDefaultTimeout(10 * 1000);
        return context;
    }

    private void clearContext(BrowserContext context)
    {
        context.clearCookies();
        for (Page p : context.pages())
        {
            p.close();
        }
        // 清除 localStorage 和 sessionStorage
        context.addInitScript("() => { localStorage.clear(); sessionStorage.clear(); }");
    }

    public static void main(String[] args)
    {
       PlaywrightUtil playwrightUtil = new PlaywrightUtil(false, 1);
       playwrightUtil.getContext().newPage().navigate("https://www.baidu.com");
    }
}
