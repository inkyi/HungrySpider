package com.inkyi.HungrySpider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.inkyi.util.BloomFilter;
import com.inkyi.util.RandomUtil;
import com.inkyi.util.SerializeUtil;

/**
 * 核心
 * @author InkYi
 *
 */
public class Spider {
	
	private Logger logger = Logger.getLogger("Spider");
	
	private final String WS_PREFIX = "http://www.";

	private final String WS_SUFFIX = ".com";
	
	private final String ROOT_PATH = "E:\\HSpider\\";

	private String websiteFile = ROOT_PATH+"website.txt";
	
	private String bfFile = ROOT_PATH+"bloomFilter.data";
	
	private String logFile = ROOT_PATH+"bloomFilter.log";
	
	private ExecutorService threadPool;
	
	private BloomFilter<String> bloomFilter;
	
	private int threadNum = 1;
	
	private Integer executes = 10000;
	
	private Integer urlLength = 6;
	
	private Integer bm = 10000000;
	
	private Integer bk = 4;
	
	/**
	 * 创建对象
	 * @author:InkYi
	 * @time:2016年8月29日 - 下午3:37:05
	 * @description:
	 * @return
	 */
	public static Spider create(){
		return new Spider();
	}
	/**
	 * 设置线程数量
	 * @author:InkYi
	 * @time:2016年8月29日 - 下午3:37:20
	 * @description:
	 * @param size
	 * @return
	 */
	public Spider thread(int num){
		if(num <= 0){
			throw new IllegalArgumentException("thread num must be greater than 0");
		}
		this.threadNum = num;
		return this;
	}
	
	/**
	 * 设置过滤器大小
	 * @author:InkYi
	 * @time:2016年8月29日 - 下午3:37:31
	 * @description:
	 * @param m
	 * @param k
	 * @return
	 */
	public Spider bloomFilter(int size,int num){
		if(size <= 0 || num <= 0){
			throw new IllegalArgumentException("bloom filter size or num must be greater than 0");
		}
		this.bm = size;
		this.bk = num;
		return this;
	}
	
	/**
	 * 设置运行次数
	 * @author:InkYi
	 * @time:2016年8月29日 - 下午3:37:58
	 * @description:
	 * @param size
	 * @return
	 */
	public Spider executes(int size){
		if(size <= 0){
			throw new IllegalArgumentException("executes must be greater than 0");
		}
		this.executes = size;
		return this;
	}
	
	/**
	 * 设置网址长度
	 * @author:InkYi
	 * @time:2016年8月29日 - 下午3:38:08
	 * @description:
	 * @param length
	 * @return
	 */
	public Spider urlLength(int length){
		if(length <= 0){
			throw new IllegalArgumentException("urlLength must be greater than 0");
		}
		this.urlLength = length;
		return this;
	}
	
	/**
	 * 生成网址
	 * @author:InkYi
	 * @time:2016年8月26日 - 上午10:03:30
	 * @description:
	 * @param size
	 * @return
	 */
	private String getWebSite(int length) {
		String website = RandomUtil.generateString(length);
		return WS_PREFIX + website.toLowerCase() + WS_SUFFIX;
	}
	
	/**
	 * 爬行
	 * @author:InkYi
	 * @time:2016年8月26日 - 上午10:02:57
	 * @description:
	 * @param uri
	 */
	private String sendGet(String url) {
		StringBuffer result = new StringBuffer();
		BufferedReader br = null;
		InputStream is = null;
		try {
			URL realUrl = new URL(url);
			URLConnection connection = realUrl.openConnection();
			connection.connect();
			is = connection.getInputStream();
			br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			String line;
			while ((line = br.readLine()) != null) {
				result.append(line);
			}
		} catch (IOException e) {
			logger.info("网址不存在！" + e);
		} finally {
			try {
				if(is!=null){
					is.close();
				}
				if(br!=null){
					br.close();
				}
			} catch (IOException e) {
				logger.info("---------流关闭错误！-------");
			}
			
		}
		return result.toString();
	}

	/**
	 * 正则查找
	 * @author:InkYi
	 * @time:2016年8月26日 - 上午10:02:13
	 * @description:
	 * @param html
	 * @return
	 */
	private boolean lookup(String html) {
		Pattern p = Pattern.compile("另类视频");
		Matcher m = p.matcher(html);
		if(m.find()){
			logger.info("------------匹配成功---------");
			System.out.println(m.group());
			return true;
		}else{
			logger.info("------------匹配失败---------");
			return false;
		}
		
	}

	/**
	 * 持久化
	 * @author:InkYi
	 * @time:2016年8月26日 - 上午10:02:01
	 * @description:
	 * @param path
	 * @param content
	 */
	private void persist(String path, CharSequence content) {
		logger.info("------------写入网址库---------");
		try {
			FileWriter fileWriter = new FileWriter(path, true);
			fileWriter.append(content);
			fileWriter.append("\n");
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	private void startLoop(){
		for (int i = 0; i < executes; i++) {
			threadPool.execute(new Runnable() {
				public void run() {
					String webSite = getWebSite(urlLength);
					if(bloomFilter.test(webSite)){
						return;
					}
					bloomFilter.add(webSite);
					String result = sendGet(webSite);
					if(result==null||result.isEmpty()){
						return;
					}
					boolean isFlag = lookup(result);
					if(isFlag){
						persist(websiteFile, webSite);
					}
				}
			});
		}
	}
	
	/**
	 * 创建目录
	 * @author:InkYi
	 * @time:2016年8月29日 - 下午5:28:57
	 * @description:
	 * @param paht
	 */
	private void buildFolder(String paht){
		logger.info("------------构建目录---------");
		File file = new File(paht);
		if(!file.exists()){
			file.mkdirs();
		}
	}
	
	/**
	 * 创建定时任务
	 * @author:InkYi
	 * @time:2016年8月29日 - 下午5:46:10
	 * @description:
	 */
	private void buildTimer(long delay,long period) {
		logger.info("------------构建定时任务---------");
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				logger.info("------------开始备份---------");
				File file = new File(bfFile);
				SerializeUtil.serialize(bloomFilter, file);
				logger.info("------------备份结束---------");
			}
		}, delay, period);
	}
	
	/**
	 * 构建线程池
	 * @author:InkYi
	 * @time:2016年8月29日 - 下午6:00:47
	 * @description:
	 * @param m
	 * @param k
	 */
	public void buildThreadPool(int num) {
		logger.info("------------构建线程池---------");
		if(threadPool==null){
			this.threadPool = Executors.newFixedThreadPool(num);
		}
	}
	
	/**
	 * 构建过滤器
	 * @author:InkYi
	 * @time:2016年8月29日 - 下午5:58:04
	 * @description:
	 * @param m
	 * @param k
	 */
	public void buildBloomFilter(int m,int k) {
		logger.info("------------构建过滤器---------");
		File file = new File(bfFile);
		if(file.isFile()){
			bloomFilter = (BloomFilter<String>) SerializeUtil.deserialize(file);
		}
		if(bloomFilter==null){
			this.bloomFilter = new BloomFilter<>(m, k);
		}
	}
	
	/**
	 * 初始化
	 * @author:InkYi
	 * @time:2016年8月29日 - 下午5:48:02
	 * @description:
	 */
	private void init() {
		logger.info("------------init bengin---------");
		buildThreadPool(threadNum);
		buildBloomFilter(bm, bk);
		buildFolder(ROOT_PATH);
		buildTimer(1000 * 60,1000 * 60 * 2);
		logger.info("------------init end---------");
	}

	/**
	 * 开始执行
	 * @author:InkYi
	 * @time:2016年8月29日 - 下午3:39:06
	 * @description:
	 */
	public void run(){
		init();
		startLoop();
	}
	
	public static void main(String[] args) {
		Spider.create().thread(10).executes(10).urlLength(6).bloomFilter(10000000, 4).run();
	}
}
