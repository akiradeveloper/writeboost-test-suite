# -*- mode: ruby -*-
# vi: set ft=ruby :

$script = <<EOF
sudo yum -y update --exclude=kernel*

wget --no-check-certificate --no-cookies - --header "Cookie: oraclelicense=accept-securebackup-cookie" http://download.oracle.com/otn-pub/java/jdk/8u65-b17/jdk-8u65-linux-x64.rpm
sudo yum localinstall -y jdk-8u65-linux-x64.rpm

curl https://bintray.com/sbt/rpm/rpm | sudo tee /etc/yum.repos.d/bintray-sbt-rpm.repo
sudo yum -y install sbt

sudo yum -y install epel-release.noarch
sudo yum -y install kernel-devel kernel-headers dkms
sudo yum -y groupinstall "Development Tools"
sudo yum -y update --exclude=kernel*
EOF

Vagrant.configure(2) do |config|
  config.vm.box = "bento/centos-7.2"
  config.vm.network "public_network"

  config.vm.synced_folder ".", "/vagrant", disabled: true
  config.vm.synced_folder ".", "/home/vagrant/dmtest"

  config.vm.provider "virtualbox" do |vb|
    vb.gui = false
    vb.cpus = 2
    vb.memory = "2048"
  end

  config.vm.provision "shell", privileged: false, inline: $script
end
