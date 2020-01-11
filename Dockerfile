# Java Version
ARG JAVA_VERSION=8

FROM openjdk:${JAVA_VERSION} AS build

ARG PAPER_VERSION=1.13.2
ARG PAPER_DOWNLOAD_URL=https://papermc.io/api/v1/paper/${PAPER_VERSION}/latest/download
ARG MINECRAFT_BUILD_USER=minecraft-build
ENV MINECRAFT_BUILD_PATH=/opt/minecraft

WORKDIR ${MINECRAFT_BUILD_PATH}

ADD ${PAPER_DOWNLOAD_URL} paper.jar

RUN useradd -ms /bin/bash ${MINECRAFT_BUILD_USER} && \
    chown ${MINECRAFT_BUILD_USER} ${MINECRAFT_BUILD_PATH} -R

USER ${MINECRAFT_BUILD_USER}

RUN java -jar ${MINECRAFT_BUILD_PATH}/paper.jar; exit 0

RUN mv ${MINECRAFT_BUILD_PATH}/cache/patched*.jar ${MINECRAFT_BUILD_PATH}/paper.jar

FROM openjdk:${JAVA_VERSION} AS runtime

ENV MINECRAFT_PATH=/opt/minecraft

WORKDIR ${MINECRAFT_PATH}

COPY --from=build /opt/minecraft/paper.jar ${MINECRAFT_PATH}/

RUN addgroup minecraft && \
    useradd -ms /bin/bash minecraft -g minecraft -d ${MINECRAFT_PATH} && \
    mkdir ${MINECRAFT_PATH}/plugins && \
    mkdir ${MINECRAFT_PATH}/worlds && \
    chown -R minecraft:minecraft ${MINECRAFT_PATH}

USER minecraft

VOLUME "${MINECRAFT_PATH}/plugins"
VOLUME "${MINECRAFT_PATH}/worlds"

EXPOSE 25565

ENTRYPOINT java -Xmx4G -server -Dcom.mojang.eula.agree=true \
    -jar ${MINECRAFT_PATH}/paper.jar \
    --world-dir=${MINECRAFT_PATH}/worlds \
    --nojline
