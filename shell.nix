with import <nixpkgs> { };
mkShellNoCC {
  buildInputs = [
    gradle
    openjdk17
  ];
#  shellHook = ''
#
#  '';
}
