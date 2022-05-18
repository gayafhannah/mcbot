with import <nixpkgs> { };
mkShellNoCC {
  buildInputs = [
    openjdk17
  ];
#  shellHook = ''
#
#  '';
}
