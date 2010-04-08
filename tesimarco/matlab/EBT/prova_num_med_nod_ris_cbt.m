clear all;
close all;
clc;


nmax=20;
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
p=0.50;
Q=zeros(nmax+1); % Q(i+1,n+1) <- Q_i(n)

for n=0:nmax
    for i=0:n
        Q(i+1,n+1)=binopdf(i,n,p);
    end
end


B=zeros(nmax+1,1);
B(1)=0;
B(2)=1;

for n=2:nmax
    s=0;
    for i=2:n-1
        s=s+Q(i+1,n+1)*B(i+1);
    end
    s=s+Q(2,n+1)*(1+B(n));
    B(n+1)=s/(1-Q(n+1,n+1)-Q(1,n+1));
end

