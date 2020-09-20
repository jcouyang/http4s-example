with import <nixpkgs> {};
mkShell {
  shellHook = ''
            source ops/bin/deps-up
            export APP_ENV=Local
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
