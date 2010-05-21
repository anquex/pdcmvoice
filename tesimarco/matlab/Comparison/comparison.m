% comparison

% vedi file test3 per biased/unbiased
%close all
clear all


figure
hold on
hold all;
%CIDON

tol=1e-10;
theta=0.99 %hard coded
%fb=inline('binocdf(k.*n.*p,n,p)-binocdf(n.*p./k,n,p)-0.99','k','n','p');
fb=inline('binocdf(floor(k.*n.*p),n,p)-binocdf(ceil(n.*p./k),n,p)-0.99','k','n','p');
fb2=inline('binocdf(floor(k.*n.*p),n,p)-binocdf(n.*p./k,n,p)-0.99','k','n','p');
C=2.623;


Ts=[0:100];
%Ts=[0:128]
batchsizes=[1000];
theta=0.99;
ii=1;

greenberg_running_time=log2(batchsizes);
%GEGA
% for n=batchsizes
%     grt=greenberg_running_time(ii);
%     K=zeros(1,length(Ts));
%     lD=0;
%     numbers=[];
%     i=1;
%     for T=Ts
%         T
%         load(['distributions/D-n-' int2str(n) '-T-' int2str(T)],'D');
%         if(lD~=length(D))
%             numbers=1:length(D);
%         end
%         k=find_min_k_given_T_and_theta(n,D,theta);
%         K(i)=k;
%         i=i+1;
%     end
%     h{ii}=plot(Ts+grt,K);
%     ii=ii+1;
% end

%CIDON
% trova k dato p
ii=1;
for n=batchsizes
    grt=greenberg_running_time(ii);
    K=zeros(1,length(Ts));
    lD=0;
    numbers=[];
    i=1;
    fractions=(Ts+grt)/n/C;
    fractions=0.005:0.001:0.15
    betterk=zeros(1,length(fractions));
    betterk2=zeros(1,length(fractions));
    for p=fractions
        p=min(p,1);
        if (p>1)
            error('p >1');
        end
        
        
        if (binocdf(0,n,p)>1-theta)
            % allora devo per forza considerare il contributo con
            % ceil(n.*p./k) =0 per cui k è infinito
            betterk(i)=-1;
        else
            a0=1;
            b0=2;
            
            if (binocdf(1,n,p)>1-theta)
                f=fb;
            else
                f=fb;
            end
            
            while (f(b0,n,p)<=0)
                f(b0,n,p)
                %pause
                %fb=inline('binocdf(floor(k.*n.*p),n,p)-binocdf(n.*p./k,n,p)-0.99','k','n','p');
                b0=b0*2;
                %%b0
            end
            disp(['intervallo considerato: [' int2str(a0) ' - ' int2str(b0)]);
            
            %fb=inline('binocdf(floor(k.*n.*p),n,p)-binocdf(ceil(n.*p./k),n,p)-
            %theta','k','n','p','theta');
            betterk(i)=fzero(@(k) fb(k,n,p),1.5);
             %   betterk(i)=bisect2(a0,b0,f,p,n,tol);
            %betterk2(i)=bisect2(a0,b0,f,p,n,tol);
        end
        i=i+1;
        %pause
    end
    h{ii}=plot(fractions,betterk);
    h1{ii}=plot(Ts,betterk2);
    ii=ii+1;
end
legend('matlab','mio')