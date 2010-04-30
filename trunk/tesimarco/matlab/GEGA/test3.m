
Ts=[0:128];
theta=0.99;
n=100
K=zeros(1,length(Ts));
Kub=zeros(1,length(Ts));
lD=0;
numbers=[];
for T=Ts
    T
    load(['D-n-' int2str(n) '-T-' int2str(T)],'D');
    if(lD~=length(D))
        numbers=1:length(D);
    end
    k=find_min_k_given_T_and_theta(n,D,theta);
    K(T+1)=k;
    %calcola bias
    avg=sum(D.*numbers);
    bias=avg/n;
    if(avg<n)
        disp(['sottostima T = ' int2str(T)]);
    else
    %correggi la distribuzione
    pos=ceil(numbers/bias);
    Dn=zeros(1,length(D));
    limit=find(D>0,1,'last');
    for i=1:limit
        Dn(pos(i))=Dn(pos(i))+D(i);
    end
    k=find_min_k_given_T_and_theta(n,Dn,theta);
    Kub(T+1)=k;
    end
end
close all
hold on
hold all
plot(Ts,K)
plot(Ts,Kub)
legend('biased','unbiased')