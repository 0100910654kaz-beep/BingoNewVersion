FROM tomcat:10.1-jdk11-corretto

# タイムゾーンを日本に設定
ENV TZ=Asia/Tokyo

# 既存のROOTアプリケーションを削除
RUN rm -rf /usr/local/tomcat/webapps/ROOT

# 作業スペースを作成
WORKDIR /app

# リポジトリ内のすべてのファイルを一旦作業スペースにコピー
COPY . .

# JSPファイルをTomcatのROOT直下に配置
RUN mkdir -p /usr/local/tomcat/webapps/ROOT && \
    find . -name "index.jsp" -exec cp {} /usr/local/tomcat/webapps/ROOT/ \; && \
    find . -name "admin.jsp" -exec cp {} /usr/local/tomcat/webapps/ROOT/ \;

# Javaファイルをコンパイルして配置するディレクトリを作成
RUN mkdir -p /usr/local/tomcat/webapps/ROOT/WEB-INF/classes

# Javaファイルをフォルダー内から自動で見つけてTomcat 10の共通ライブラリを使ってコンパイル
RUN find . -name "*.java" | xargs javac -classpath "/usr/local/tomcat/lib/*" -d /usr/local/tomcat/webapps/ROOT/WEB-INF/classes

# ポート番号の設定（Render用）
EXPOSE 8080
CMD ["catalina.sh", "run"]
