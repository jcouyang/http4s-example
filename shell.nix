with import <nixpkgs> {};
mkShell {
  shellHook = ''
            source ops/bin/deps-up
            sbt 'db/run migrate'
            cat ops/sbt-usage.txt
            '';
  buildInputs = [
    sbt
    coursier
    docker
    docker-compose
  ];
}
