# ベースイメージとして、大山さんのコード（javax.servlet）と互換性があるTomcat 9環境を指定
FROM tomcat:9.0-jdk11-corretto

# TomcatのデフォルトのWelcomeページを削除し、クラス配置用のフォルダを作成
RUN rm -rf /usr/local/tomcat/webapps/ROOT && \
    mkdir -p /usr/local/tomcat/webapps/ROOT/WEB-INF/classes

# 作業ディレクトリを /app に設定
WORKDIR /app

# リポジトリ内のすべてのファイルをコンテナの /app にコピー
COPY . .

# JSPファイルをTomcatの公開ディレクトリ（ROOT直下）に配置
RUN find . -name "index.jsp" -exec cp {} /usr/local/tomcat/webapps/ROOT/ \; && \
    find . -name "admin.jsp" -exec cp {} /usr/local/tomcat/webapps/ROOT/ \;

# JavaファイルをTomcatの共通ライブラリを使ってコンパイルし、classes直下に配置
RUN find . -name "*.java" | xargs javac -classpath "/usr/local/tomcat/lib/*" -d /usr/local/tomcat/webapps/ROOT/WEB-INF/classes

# 【カード表示のための重要設定】セッション（記憶部屋）のクッキーパスをEclipse互換に強制変更
RUN sed -i 's/<Context>/<Context sessionCookiePath="\/">/' /usr/local/tomcat/conf/context.xml

# コンテナ起動時にTomcatサーバーを走らせる
CMD ["catalina.sh", "run"]
