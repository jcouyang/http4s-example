with import <nixpkgs> {};
mkShell {
  shellHook = ''
              echo "usage:"
              echo "  sbt ~reStart       start server at port 8080(default)"
              echo "  sbt testQuick      run test"
              echo "  sbt rmUnused       remove unused imports"
              echo "  sbt bootstrap      create a runnable binary at current folder"
            '';
  buildInputs = [
    sbt
    coursier
  ];
}
