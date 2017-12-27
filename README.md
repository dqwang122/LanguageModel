# Language Model

[TOC]



## 一、任务描述

利用Map Reduce框架，根据中文语料库，构建Trigram Language Model。

最基本的Trigram Language Model直接计算P(W3|W1W2)，改进版本使用 Kneser-Ney Smoothing算法进行平滑。

最后基于该Language Model实现汉字预测。　

<br />

### 1.1 任务完成度

完成度：

1. 基本Trigram Language Model
2. Kneser-Ney Smoothing平滑算法
3. 基于Trigram和Bigram Language Model的汉字预测。


<br />

---

<br />

### 1.2 中文语料库

中文语料库为搜狗新闻SogouNews，来源于10个新闻网站的新闻页面，是自然语言处理常使用的一个中文数据库。

本次实验使用的语料文件为news_tensite_xml.full.tar.gz，大小为712M，解压后news_tensite_xml.dat大小为1.5G。文件格式为xml，包含网页中\<a\>\<p\>等标签，编码方式为GBK，所有字符全部为全码格式。

#### 1.2.1 文本提取

对原始数据进行简单提取，仅仅保留\<content\>标签下新闻正文内容，删除空行，并且将GBK编码转换成UTF-8编码，得到的news.txt文件大小为1.8G。

`$ cat news_tensite_xml_partition_ac | iconv -c -f GB18030 -t utf-8 |grep "<content>" | sed 's\<content>\\' | sed's\</content>\\'  | sed '/^$/d'> news.txt `

#### 1.2.2 数据处理

提出的纯文本包含汉字、字母、数字和标点符号，所有字符全部为全码方式编码。

通过正则表达式匹配去除文本中的字母和数字，只保留汉字和标点符号。将标点符号视作特殊的汉字字符，包含在Language Model的训练中。

```java
public static String GetChineseWord(String str_text) {
    StringBuilder builder = new StringBuilder();
    // 0-9 or chinese words or Punctuation
    String reg="([\uFF10-\uFF19|\u4e00-\u9fa5|\u3002\uff1b\uff0c\uff1a\u201c\u201d\uff08\uff09\u3001\uff1f\u300a\u300b]+)";
    Matcher matcher = Pattern.compile(reg).matcher(str_text);
    while(matcher.find()){
        builder.append(matcher.group());
    }
    return builder.toString();
}
```

<br />

---

<br />

### 1.3 Trigram Language Model

N-Gram（有时也称为N元模型）指的是文本中连续的N个item（item可以是phoneme,syllable, letter, word或base pairs）

n-gram中如果n=1则为unigram，n=2则为bigram，n=3则为trigram。n>4后，则直接用数字指称，如4-gram，5gram。

N-gram Language Model是利用前N-1个词语来预测第N个词语，即
$$
W_N = max_w(P(W|W_1W_2...W_{N-1}))
$$
其中$P(W|W_1W_2...W_{N-1})$是根据已有语料库计算得到的：

$$
P(W|W_1W_2...W_{N-1}) = \frac{C_{W_1W_2...W_{N-1}W_N}}{\sum_{w}C_{W_1W_2...W_{N-1}w}}=\frac{C_{W_1W_2...W_{N-1}W_N}}{C_{W_1W_2...W_{N-1}}}
$$
其中$C_{W_1W_2...W_{N-1}W_N}$表示$W_1W_2...W_{N-1}W_N$出现的个数。

根据公式(2)可知，当$C_{W_1W_2...W_{N-1}}$为0，也就是$W_1W_2...W_{N-1}$没有在语料库中出现过，概率计算会出现分母为0的情况。为了避免这种情况，往往会使用平滑(smoothing)。

平滑算法有很多，本实验采用当下Kneser-Ney Smoothing算法。

<br />

#### 1.3.1 Smoothing

Smoothing一般有两种大的方向

1. 为了避免没有出现在语料库的词语组合Count为0，给所有词语组合$W_1W_2...W_{N-1}$的Count一个基数bias。本质是重新分配词语组合概率，将高概率词语的概率匀给没有出现的词语。
   - Add-one smoothing / Add-K smoothing
     - 为了避免$C_{W_1W_2...W_{N-1}}$为0，给每个$C_{W_1W_2...W_{N-1}W_N}$加上一个数k，分母相应增加，从而避免分母为0.
   - Absolute Discount
     - 对所有非零的Count，都减去一个固定的值d，分母不变
     - 直觉上理解就是对于Count大的词语，已经得到一个比较好的概率估计P，减去d之后可以将概率匀给Count小的
     - 此外还需要Interpolation模型
2. 使用插值(interpolation)或者回退(backoff)，结合高阶模型和低阶模型信息。
   - Backoff：在高阶不存在时，依次使用低阶模型
   - Interpolation：高阶模型和低阶模型按照权重相加  


<br />


#### 1.3.2 Kneser-Ney Smoothing

##### 1.3.2.1 Continuation Count

Kneser-Ney Smoothing 是对 Absolute Discount 的一个改进

Absolute Discount公式为：
$$
P (w_N│w_1w_2..w_{N-1} )=\frac{max⁡(0,C (w_1w_2...w_{N} )−d)}{C (w_1w_2...w_{N-1} )}+λ(w_1w_2...w_{N-1} )∗P (w_{N}│w_2...w_{N-1} )
$$

- d是一个固定的discount系数，一般取0.75
- $λ(w_1w_2...w_{N-1} )$是(n-1)-gram的插值权重
- $P (w_{N}│w_2...w_{N-1} )$是(n-1)-gram模型  



Kneser-Ney Smoothing的改进之处主要在于unigram的计算方式上，使用Continuation Count替代原本单纯的Count。

一般的unigram计算$P(w)=\frac{w}{\sum_i{w_i}} $，完全根据单词w出现的个数Count计算。但是对于有固定搭配的词组$w_{i-1}w_i$，如果$w_{i-1}w_i$出现的个数很多，那么相对应的$w_i$的概率也很大，但是有可能$w_i$单独出现的概率其实很小。

比如地名"San Francisco"，如果在语料中反复出现，那么"Francisco"的unigram概率会很大，但是实际上"Francisco"几乎不单独出现。

<br />

为了解决这个问题，Kneser-Ney Smoothing提出结合历史信息$w_{i-1}$对$w_i$的unigram概率$P(w_i)$的改进$P_{continuation}(w_i)$：
$$
P_{continuation}(w_i) = \frac{C_{endwith}(w_i)}{\sum_{w_k}C_{endwith}(w_k)} = \frac{C_{endwith}(w_i)}{C_{unique}(w_k)}\\
 = \frac{{|\{w_{i-1}|C_{w_{i-1}w_i}>0\}|}}{{|\{(w_{i-1}w_i)|C_{w_{i-1}w_i}>0\}|}}
$$

* $C_{endwith}(w_i)$表示以$w_i$结尾的所有的bigram种类个数，即集合$\{w_{i-1}|C_{w_{i-1}w_i}>0\}$的元素个数
* $\sum_{w_k}C_{endwith}(w_k)$表示对$w_k$求和的bigram种类个数，也就是所有不重复的bigram组合的种类个数，用集合表示就是$\{(w_{i-1}w_i)|C_{w_{i-1}w_i}>0\}$的元素个数


<br />


$P_{continuation}(w_i)$直观理解是$w_i$作为一个词语Continuation的概率。在前一个词语未知的时候(使用unigram进行预测的假设是只知道当前词语)，下一个词语是$w_i$的概率，换个角度理解可以是$w_i$作为单独单词出现的概率：

- 当能够和$w_i$作为bigram出现的$w_{i-1}$种类越多，说明$w_i$越百搭，也就是作为一个任意$w_{i-1}$的continuation概率就很高。
- 当能够和$w_i$作为bigram出现的$w_{i-1}$种类越少，说明$w_i$有固定的搭配用法，也就是在除了该用法之外的情况下很少出现，在作为一个任意词语的continuation的unigram计算的时候概率小。

需要注意的是，虽然利用了历史信息，但是$P_{continuation}(w_i)$本身仍然是一个unigram模型。



因此，Kneser-Ney Smoothing在计算bigram时公式为：
$$
P (w_2│w_1 )=\frac{max⁡(0,C (w_1w_2)−d)}{C (w_1)}+λ(w_1 )∗P_{continuecount}(w_2 )
$$
其中$P_{continuecount}(w_2)$按照公式(4)计算。

<br />

##### 1.3.2.2 Kneser-Ney Smoothing

将上述Continuation Count应用于所有n-gram的计数上，把所有$C(w_1w_2...w_N)$替换成$C_{KN}(w_1w_2...w_N)$可以得到一个泛化公式：
$$
P_{continuation}(w_N│w_1w_2..w_{N-1} )=\frac{max⁡(0,C_{KN}(w_1w_2...w_{N} )−d)}{C_{KN}(w_1w_2...w_{N-1} )} \\
+ λ(w_1w_2...w_{N-1})∗P_{continuation}(w_{N}│w_2...w_{N-1} )
$$


其中$λ(w_1w_2...w_{N-1})$是(N-1)-gram的插值系数：
$$
λ(w_1w_2...w_{N-1}) = \frac{d}{C_{unique}(w_1w_2...w_{N-1})} * C_{startwith}(w_1w_2...w_{N-1})\\
=  \frac{d}{\sum_w(|\{C_{KN}(w_1w_2...w_{N-1}w)>0\}|)} * |\{w:C_{KN}(w_1w_2...w_{N-1}w)>0\}|
$$


这里用$C_{KN}$表示Continuation Count，需要利用前一个单词的历史信息。因为以w1开始的Continuation Count没有可以利用的历史信息，因此对于w1需要特殊处理：
$$
C_{KN}(w_i...w_n)=\left\{
  \begin{aligned}
  C(w_1w_2...w_n),  && i = 1 \\
  C_{continuation}(w_iw_{i+1}...w_n) ， && i > 1 \\
  \end{aligned}
\right.
$$

<br />

##### 1.3.2.3 Trigram Language Model with Smoothing
根据上述公式，可以推出Trigram Language Model的Kneser-Ney Smoothing的计算公式：

Trigram
$$
P_{KN}(w_3│w_1,w_2 )=\frac{max⁡(0,C_{KN}(w_1,w_2,w_3 )−d)}{C_{KN} (w_1,w_2 ) }+λ(w_1,w_2 )∗P_{KN} (w_3│w_2)
$$

- $\lambda(w_1,w_2 )=\frac{d}{C_{KN}(w_1,w_2)}∗C_{startwith}(w_1,w_2)$
- $C_{KN}(w_1,w_2,w_3)=C(w_1,w_2,w_3)$
- $C_{KN}(w_1,w_2)= \sum_{(w_3)}(|C(w_1,w_2,w_3)|)$




<br />

Bigram
$$
P_{KN}(w_3│w_2)=\frac{max⁡(0,C_{KN}(w_2,w_3)−d)}{C_{KN}(w_2)}+\lambda(w_2)∗P_{KN}(w_3)
$$

- $\lambda(w_2) =\frac{d}{C_{KN} (w_2)}∗C_{startwith}(w_2)​$
- $C_{KN}(w_2,w_3 )=C_{continuation}(w_2,w_3) = C_{endwith}(w_2,w_3)$
- $C_{KN} (w_2)=C_{continuation}(w_2)=C_{middle}(w_2)=\sum_{w_3}(C_{endwith}(w_2,w_3))$




<br />

Unigram
$$
P_{KN}(w_3)=P_{continuation}(w_3)=\frac{C_{endwith}(w_3)}{\sum_{w_3}C_{endwith}(w_3)}
$$

<br />

---

<br />



### 1.4 汉字预测
根据上述计算的Trigram Language Model进行汉字预测

思路：对于任意输入字符串，选取最后两个字符作为$w_1w_2$，然后计算$P(w_3|w_1w_2)$，将概率由高到低排序作为候选字

#### 1.4.1索引
1. 事先对每个在语料库Trigram中出现过的$w_1w_2$，选择10个出现次数最高的$w_3$作为候选字列表。
2. 如果出现过的$w_1w_2$，其候选的$w_3$不满10个，则添加bigram文件中$w_2$的候选字，直到满10个。
3. 计算smoothing之后的$P(w_3|w_1w_2)$，按照概率由高到低存储在索引文件index中。



#### 1.4.2 搜索 
1. 对于输入的$w_1w_2$，首先搜索index文件，如果有对应条目，直接取出前5个。
2. 如果不存在，那么对使用最频繁的十个汉字("的"、"一"、"了"、"是"、"我"、"不"、"在"、"人"、"们""有")作为$w_3$，分别计算$P(w_3|W_1W_2)$，选择概率最大的5个输出。


<br />

<br />





## 二、实现架构

### 2.1. Trigram Language Model

Trigram Language Model 需要计算
$$
P(w_3|w_1w_2) = \frac{C(w_1w_2w_3)}{\sum_{w}C(w_1w_2w)}
$$
因此需要计算$(w_1w_2,w_3)$在语料库中出现的次数。因为语料库的数据量大，因此使用Hadoop架构实现计数。



#### 2.1.1 Baseline 

Mapper: $(docno, doc) \to (ab,c)$,

Reducer:  $(ab,*)\to ab:\{c_1:1, c_2:5....\}$

Mapper对于每个Trigram都emit一次。Reducer对所有$(ab, *)$计数，保存成List形式。在计数时候保存一个sum，计数结束之后再根据公式(11)计算概率P。


```Java
/* Parts of the code */
public void map(LongWritable key, Text value, Context context){
  String words = GetChineseWord(value.toString());
  for(int i = 0; i < words.length() - 2; i++){
    String bigram = words.substring(i, i+2);    /* (W1,W2) */
    Text ch = new Text(words.substring(i+2, i+3));  /* W3 */
    context.write(new Text(bigram), ch);
  }
}

public void reduce(Text key, Iterable<MapWritable> value, Context context)
  HashMap<String, Integer> stripe = new HashMap<>();
  double sum = 0;
  /* for combining different mapper with the same key (W1, W2) */
  for (MapWritable val : value) {
    if(val.getLength()!=0) { 	/* for each element W3 */
      IntWritable cnt = new IntWritable(1);
      if(stripe.containsKey((val))) {
        stripe.put(val, stripe.get(val) + 1);
      }
      else {         
        stripe.put(val, cnt);
      }
    }
  }
  // Some actions to convert stripe to String stripestr
  context.write(key, stripestr);
}
```

<br />

#### 2.1.2 Stripes

Mapper: $(docno, doc) \to  (ab,\{c_1:1, c_2:1....\})$,

Reducer:  $ (ab,\{c_1:1, c_2:1....\}) \to ab:\{c_1:1, c_2:5....\}$

Mapper在遍历doc时，将ab保存在HashTable，其value是关于c的一个List。对于$(ab,*)$ 首先查找ab是否在HashTable中，然后给对应条目的List增加新的c 或 给已有的c增加计数。

Recuder对ab的所有关于c的List进行合并，保存成一个整个语料库范围内的List。同样按照上述方式计算P。


```Java
/* Parts of the code */
public void map(LongWritable key, Text value, Context context){
  /* HashTable for (W1,W2)*/
  HashMap<String, MapWritable> stripes  = new HashMap<>();    
  String words = GetChineseWord(value.toString());
  for(int i = 0; i < words.length() - 2; i++){
    String bigram = words.substring(i, i+2);    /* (W1,W2) */
    Text ch = new Text(words.substring(i+2, i+3));  /* W3 */
    MapWritable bigramList;     /* (W1,W2) -> {W3:1,W3:3,....} */
    
    // Some Actions to get bigram from HashTable or Add bigram to HashTable
    IntWritable cnt_tmp = new IntWritable(1);
    if(bigramList.containsKey(ch)){
      IntWritable cnt = (IntWritable) bigramList.get(ch);
      cnt_tmp.set(cnt.get() + 1);
    }
    bigramList.put(ch, cnt_tmp);
  }
  /* emit */
  for(String bigram : stripes.keySet()){
    context.write(new Text(bigram), stripes.get(bigram));
  }
}

public void reduce(Text key, Iterable<MapWritable> value, Context context)
  HashMap<String, Integer> stripe = new HashMap<>();
  double sum = 0;
  /* for combining different mapper with the same key (W1, W2) */
  for (MapWritable val : value) {
    if(!val.isEmpty()) {
      for (Writable w : val.keySet()) { /* for W3 */
        int cnt = ((IntWritable)val.get(w)).get();
        String wstr = (w).toString();
        /* record the total number of (W1,W2) */
        sum += cnt;
        if(stripe.containsKey((wstr))) {
          cnt += stripe.get(wstr);
        }
        stripe.put(wstr, cnt);
      }
    }
  }
  // Some actions to convert stripe to String stripestr
  context.write(key, stripestr);
}
```

<br />

#### 2.1.3 Stripes with Combiner

Mapper: $(docno, doc) \to  (ab,\{c_1:1, c_2:1....\})$,

Combiner: $ (ab,\{c_1:1, c_2:1....\}) \to ab:\{c_1:1, c_2:5....\}$

Reducer:  $ (ab,\{c_1:1, c_2:1....\}) \to ab:\{c_1:1, c_2:5....\}$

和Stripes的方法相同，增加了Combiner，进行本地的合并再提交。

Combiner执行的代码和Reducer基本一样。

<br />

---

<br />

### 2.2 Kneser-Ney Smoothing

因为Kneser-Ney Smoothing中需要对bigram和trigram进行计数，故同样需要用到Hadoop实现计算并且存储成文件，以便计算KNS时候读取。

#### 2.2.1 Hadoop 预处理

根据公式(8),(9)和(10)，需要事先计数有：

1. $C_{startwith}(w_1w_2)$, $count(w_1,w_2,w_3)$,$\sum_{w_3}count(w_1,w_2,w_3)$ 
2. $C_{endwith}(w_2w_3)$, $\sum_{w_3}C_{endwith}(w_2w_3)$
3. $C_{startwith}(w_2)$, $C_{endwith}(w_1w_2)$,$\sum_{w_3}C_{endwith}(w_3)$

第1项可以直接从上述Trigram Language Model中衍生出来，只要不进行概率的计算即可。生成文件为LM_Trigram
第2项需要计算以$w_2w_3$结尾的不相同的trigram个数，并且需要对$w_3$求和。

- 保存格式为$w_2:\{w_3:2, w_3':3...\}$
- 其中$w_3$的value不是$(w_2w_3)$的个数，而是$(w_1w_2w_3)$中$w_1$的种类。、
- 生成文件为LM_Trigram_end



第3项需要计算以$w_2$开头，以$w_3$结尾的bigram个数，并且对$w_3$求和。最后一项对$w_3$求和本质是不同bigram的总个数。

- 保存格式为$w:\{start:2, end:3\}$
- 其中start表示以w开头的不同bigram个数，end表示以w结尾的不同bigram个数
- 文件第一行记录总的bigram个数。
- 生成文件为LM_Bigram


<br />

#### 2.2.2 KNS算法

根据公式(8),(9)和(10)，主要需要计算的有$P_{KN}$, $\lambda$和$C_{KN}$
其中$P_{KN}$递归进行，当unigram时候计算的是$P_{continuation}$
在实现时封装KNS类，使用Pkn, CalLambda, Ckn和Pcontinue函数分别计算上述公式。

KNS类有两种初始化方式

1. 待求字符串$w_1w_2w_3$和Hadoop生成的LM_Trigram, LM_Trigram_end, LM_Bigram文件路径
2. 待求字符串$w_1w_2w_3$和已经加载到内存的TrigramTable, TrigramEndTable,LM_Bigram

一个字符串$w_1w_2w_3$平均计算耗时是4s左右。

<br />

---



<br />

### 2.3 汉字预测

为了加快预测速度，最好首先建立索引文件，保存对于$w_1w_2$组合已经计算好的候选字$w_3$列表。

1. 对语料库中所有出现过的$w_1w_2$，将其$w_3$的List按照频率出现由高到低排序，选择前10个保存为LM_Trigram_query。对bigram进行相同处理，保存为LM_Bigram_query，以便在LM_Trigram_query不满10个时候补充。为了加快计算速度，该过程同样放在Hadoop分布式架构中完成，在Mapper中排序选取，Reducer直接输出。
2. 然后根据LM_Trigram_query，对$w_1w_2$，结合LM_Bigram_query的补充，计算KNS平滑之后的$P(w_3|w_1w_2)$，按照频率大小排序。

由于步骤2计算量较大，KNS平滑需要访问LM_Trigram, LM_Trigram_end, LM_Bigram文件并查找，因此尝试将三个文件通过HashTable加载到内存中，但是内存消耗过大无法运行。放在Hadoop框架下，在Mapper的Setup加载同样遇到内存过大问题。
又尝试使用Java的Fork/Join并行计算框架，速度提升效果不明显。
最后由于时间限制，没有完成所有Trigram的索引任务。部分完成结果放在LM_Trigram_query_candidate文件中。

<br />

<br />

## 三、程序启动与操作说明

### 3.1 项目目录

```
Project                      
├── src/main/java		# 源代码
│	├── DataProcess.java 	# 数据清理相关函数	    
│	├── KNS.java 			# KNS平滑算法
│	├── LanguageModel.java 	# Hadoop架构的计算
│	├── ParaTask.java 		# Fork/Join架构计算索引文件
│	├── Query.java 			# 汉字预测主程序
│	└── ReadFromFile.java	# 读取文件相关函数	    
├── result				# Hadoop生成数据文件
│	├── LM_Bigram			# KNS的Bigram数据				    
│	├── LM_Bigram_query 	# Bigram的候选字列表
│	├── LM_Trigram		 	# KNS的Trigram数据
│	├── LM_Trigram_end	 	# KNS的Trigram_end数据
│	├── LM_Trigram_query 	# Trigram的候选字列表
│	├── LM_Trigram_query_candidate 	# 汉字预测的索引文件
│	└── LM_without_smoothing	# 基本的$P(w_3|w_1w_2)$
└── README.md
```

<br />

### 3.2 程序运行

在根目录下运行Query类（确保result文件的路径正确）
Query类存在两种预测模式fast和precise。

- fast直接根据$P(w_3|w_1w_2)$进行预测，也就是直接从LM_Trigram_candidate和LM_Bigram_candidate中选择候选。速度较快。
- precision根据KNS平滑之后进行预测。首先尝试从索引文件LM_Trigram_query_candidate中查找，如果找不到，则通过候选进行计算。速度较慢。

Query运行之后，首先选择模式，默认是fast模式。
模式选择完之后，进入汉字预测阶段。根据输入字符串的最后2个字符预测，如果字符串长度小于2，则要求重新输入。
预测结果包括5个候选项，按照可能性从高到低排序。此外显示本次计算时间。
输入"q"或"Q"或"exit"退出。

<br />

<br />

## 四、项目结果

### Hadoop性能比较

在计算最基本的Trigram Language Model时，实验对比了三种方式在小数据(单机模式)和大数据(分布式环境)下的时间性能。对比如下：

|                       | 单机模式     | 分布式模式         |
| --------------------- | -------- | ------------- |
| 数据集规模                 | 1.1M     | 1.76G         |
| Baseline              | 7.621 s  | Out of Memory |
| Stripes               | 10.322 s | 1631.527 s    |
| Stripes with Combiner | 12.783 s | 568.632 s     |

<!--在Baseline方法下，因为所有Trigram都是以$(w_1w_2, w_3)$的pair方式从Mapper发送到Reducer，Reducer接收过多pair导致内存不够。-->

从表中可以发现，在单机模式的小数据下，三种方法所消耗的时间依次增多。然而在分布式的大数据模式下，消耗的时间依次减少。
分析Map-reduce过程，时间瓶颈主要在于Map处理数据和emit的通信频率，内存瓶颈主要在于Reduce收到相同key的value的大小。
在单机模式下，emit的代价基本可以忽略，Map数据处理所花费的时间占大部分。因此最简单的baseline表现最好，而Stripes需要HashTable的存储和List的更新，反而消耗时间。而Combiner在单机模式下其实和Reducer作用差不多，并且在小数据上需要合并的key很少。根据Log可以看出，Combiner和Reducer的输出相差无几，但是Combiner却增加了对所有key遍历，因此耗时更久。

> 17/12/20 22:06:36 INFO mapred.JobClient:     Combine output records=107284 <br />
> 17/12/20 22:06:36 INFO mapred.JobClient:     Spilled Records=321852 <br />
> 17/12/20 22:06:36 INFO mapred.JobClient:     Reduce input records=107284 <br />
> 17/12/20 22:06:36 INFO mapred.JobClient:     Reduce output records=103747 <br />

在分布式模式下，emit的代价不可以忽略，占据Map的耗时的大部分。并且在大数据下，内存称为至关重要的瓶颈。在实验中，Baseline的Map过程完成很迅速，但是Reduce速度很慢，在70%左右时内存耗尽。同时，因为分布式部署，本地的Combine对速度提升非常有效，加上Combiner之后速度加快3倍以上。

<br />

---

<br />

## Reference

[1]自然语言处理中N-Gram模型的Smoothing算法 : http://blog.csdn.net/baimafujinji/article/details/51297802

[2]NLP 笔记 - 平滑方法(Smoothing)小结：[http://www.shuang0420.com/2017/03/24/NLP%20笔记%20-%20平滑方法(Smoothing)小结/](http://www.shuang0420.com/2017/03/24/NLP%20笔记%20-%20平滑方法(Smoothing)小结/)

[3]Kneser-Ney Smoothing: http://smithamilli.com/blog/kneser-ney/