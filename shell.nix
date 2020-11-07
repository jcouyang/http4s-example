with import <nixpkgs> {};
mkShell {
  shellHook = ''
            set -a
            source app.env
            set +a
            source ops/bin/deps-up
            sbt 'db/run migrate'
            cat ops/sbt-usage.txt
            set +e
            '';
  buildInputs = [
    sbt
    coursier
    docker
    docker-compose
  ];
}
