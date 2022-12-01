function generateRouters() {
  let output = "";
  for (let i = 1; i <= 5; i++) {
    output += `${i > 1 ? "\n" : ""}  R${i}:
    container_name: R${i}
    image: openjdk:19-jdk-alpine
    working_dir: /src
    volumes:
      - ./src:/src
    networks:
      flow-forwarding:
        ipv4_address: 192.168.10.${1 + i}0
    command: java -cp . Router
    depends_on:
      - controller`;
  }
  return output;
}

function generateEndNodes() {
  let output = "";
  for (let i = 1; i <= 4; i++) {
    output += `${i > 1 ? "\n" : ""}  E${i}:
    container_name: E${i}
    image: openjdk:19-jdk-alpine
    working_dir: /src
    volumes:
      - ./src:/src
    networks:
      flow-forwarding:
        ipv4_address: 192.168.10.${6 + i}0
    command: tail -f /dev/null
    depends_on:
      - controller`;
  }
  return output;
}

function generateDockerCompose() {
  let output = "";
  output += `version: "2"
services:
  controller:
    container_name: controller
    image: openjdk:19-jdk-alpine
    working_dir: /src
    volumes:
      - ./src:/src
    networks:
      flow-forwarding:
        ipv4_address: 192.168.10.10
    command: java -cp . Controller
# ROUTERS
${generateRouters()}

# END NODES
${generateEndNodes()}

# TCP DUMP
  tcp_dump:
    image: kaazing/tcpdump
    network_mode: "host"
    volumes:
      - ./tcpdump:/tcpdump

networks:
  flow-forwarding:
    ipam:
      driver: default
      config:
        - subnet: 192.168.10.0/24
          gateway: 192.168.10.1`;

  return output;
}

/**
 * MAIN
 */

console.log(generateDockerCompose());
