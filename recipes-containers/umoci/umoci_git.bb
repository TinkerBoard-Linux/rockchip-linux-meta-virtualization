HOMEPAGE = "https://github.com/openSUSE/umoci"
SUMMARY = "umoci modifies Open Container images"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://COPYING;md5=3b83ef96387f14655fc854ddc3c6bd57"

RDEPENDS_${PN} = "skopeo"
RDEPENDS_${PN}_class-native = ""

SRCREV_umoci = "758044fc26ad65eb900143e90d1e22c2d6e4484d"
SRC_URI = "git://github.com/opencontainers/umoci.git;branch=master;name=umoci;destsuffix=github.com/opencontainers/umoci \
          "

PV = "v0.4.7-dev+git${SRCPV}"
S = "${WORKDIR}/github.com/opencontainers/umoci"
GO_IMPORT = "github.com/opencontainers/umoci"

inherit goarch
inherit go

# This disables seccomp and apparmor, which are on by default in the
# go package. 
EXTRA_OEMAKE="BUILDTAGS=''"


do_compile_class-native () {
    export GOARCH="${BUILD_GOARCH}"

    # Pass the needed cflags/ldflags so that cgo can find the needed headers files and libraries
    export CGO_ENABLED="1"
    export CFLAGS="${BUILD_CFLAGS}"
    export LDFLAGS="${BUILD_LDFLAGS}"
    export CGO_CFLAGS="${BUILD_CFLAGS}"

    # as of go 1.15.5, there are some flags the CGO doesn't like. Rather than
    # clearing them all, we sed away the ones we don't want.
    # export CGO_LDFLAGS="$(echo ${BUILD_LDFLAGS} | sed 's/-Wl,-O1//g' | sed 's/-Wl,--dynamic-linker.*?( \|$\)//g')"
    export CC="${BUILD_CC}"
    export LD="${BUILD_LD}"

    export GOPATH="${WORKDIR}/git/"
    export GO111MODULE=off

    cd ${S}

    # why static ? patchelf will be run on dynamic binaries and it breaks
    # the executable (coredump)
    # https://forum.snapcraft.io/t/patchelf-broke-my-binary/4928
    # https://github.com/NixOS/patchelf/issues/146
    oe_runmake umoci.static

    # so the common install can find our binary
    cp umoci.static umoci
}

do_compile() {
    export GOARCH="${TARGET_GOARCH}"
    export GOPATH="${WORKDIR}/git/"

    # Pass the needed cflags/ldflags so that cgo
    # can find the needed headers files and libraries
    export CGO_ENABLED="1"
    export CFLAGS=""
    export LDFLAGS=""
    export CGO_CFLAGS="${BUILDSDK_CFLAGS} --sysroot=${STAGING_DIR_TARGET}"
    export CGO_LDFLAGS="${BUILDSDK_LDFLAGS} --sysroot=${STAGING_DIR_TARGET}"

    export GO111MODULE=off

    cd ${S}

    oe_runmake umoci
}

do_install() {
    install -d ${D}/${sbindir}
    install ${S}/umoci ${D}/${sbindir}
}

INSANE_SKIP_${PN} += "ldflags already-stripped"
BBCLASSEXTEND = "native nativesdk"
